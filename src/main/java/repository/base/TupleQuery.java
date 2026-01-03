package repository.base;

import java.lang.reflect.InvocationTargetException;
import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
// import org.apache.commons.lang3.StringUtils; // Removido - não disponível no WildFly

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Tuple;
import jakarta.persistence.TupleElement;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.From;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;



public class TupleQuery<E> implements BaseQuery<E> {

    private final EntityManager entityManager;
    private final Class<E> entityClass;

    private Class dtoClass;

    private final CriteriaBuilder criteriaBuilder;
    private final CriteriaQuery<Tuple> criteriaQuery;

    private List<String> selectPart;
    private List<String> groupByPart;

    private final Root<E> root;
    private List<Join> joins;
    private final List<Predicate> predicates = new ArrayList<>();

    // Predicados usados entre o beginOr() e endOr()
    private final List<Predicate> orPredicates = new ArrayList<>();
    private boolean insideOr;

    private Integer firstResult;
    private Integer maxResults;

    private List<Order> orders = new ArrayList<>();

    private TupleQuery(EntityManager entityManager, Class<E> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.criteriaQuery = criteriaBuilder.createTupleQuery();
        this.root = criteriaQuery.from(entityClass);
        this.selectPart = new ArrayList<>();
        this.groupByPart = new ArrayList<>();
        this.joins = new ArrayList<>();
    }

    public static <T> TupleQuery<T> create(EntityManager entityManager, Class<T> entityClass) {
        return new TupleQuery<>(entityManager, entityClass);
    }

    public TupleQuery<E> select(String ... select) {
        this.selectPart.addAll( Arrays.asList(select));
        return this;
    }

    @Override
    public List<Tuple> list() {
        TypedQuery<Tuple> typedQuery = prepareSelectTypedQuery();

        if (firstResult != null) {
            typedQuery.setFirstResult(firstResult);
        }

        if (maxResults != null) {
            typedQuery.setMaxResults(maxResults);
        }

        return typedQuery.getResultList();
    }

    public <T> List<T> list(Class<T> clazz) {
        List<Tuple> tuples = this.list();
        try {
            List<T> lista = new ArrayList<>(tuples.size());
            for ( Tuple t: tuples ) {
                lista.add( createAndPopulateBean(clazz, t) );
            }
            return lista;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new PersistenceException("Erro ao criar DTO", e);
        }
    }

    public Tuple uniqueResult() {
        TypedQuery<Tuple> typedQuery = prepareSelectTypedQuery();
        return typedQuery.getSingleResult();
    }

    private TypedQuery<Tuple> prepareSelectTypedQuery() {
        List<Selection> path = new ArrayList<>();
        for (String s: this.selectPart ) {
            path.add( toJpaPath(s).alias(s) );
        }

        if (criteriaQuery.isDistinct()) {
            List<Order> orders = criteriaQuery.getOrderList();

            if (CollectionUtils.isNotEmpty(orders)) {
                for (Order order : orders) {
                    path.add(order.getExpression());
                }
            }
        }

        criteriaQuery.multiselect(path.toArray(new Selection[path.size()]));
        criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

        if ( this.groupByPart.size() > 0 ){
            List<Expression<?>> pathGroup = new ArrayList<>();
            for (String s: this.groupByPart ) {
                pathGroup.add( toJpaPath(s) );
            }
            criteriaQuery.groupBy(pathGroup);
        }

        if (orders.size() > 0) {
            criteriaQuery.orderBy(orders);
        }

        return entityManager.createQuery(criteriaQuery);
    }

    @Override
    public TupleQuery<E> innerJoinFetch(String attribute) {
        root.fetch(attribute, JoinType.INNER);
        return this;
    }

    @Override
    public TupleQuery<E> join(String attribute) {
        this.join(attribute, JoinType.INNER);
        return this;
    }

    @Override
    public TupleQuery<E> leftJoin(String attribute) {
        this.join(attribute, JoinType.LEFT);
        return this;
    }

    public TupleQuery<E> rightJoin(String attribute) {
        this.join(attribute, JoinType.RIGHT);
        return this;
    }

    private void join(String attribute, JoinType type) {
        if (attribute.indexOf('.') != -1) {
            String[] paths = attribute.split("\\.");

            From<?, ?> join = root;
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];

                // Reusa join "pai" (só suportra 2 níveis: ex. ultimaMovimentacao.motivo), onde o pai é a "ultimaMovimentacao".
                // TOOD: se necessário, fazer algo que suporte mais de 2 níveis.
                if (paths.length == 2 && i == 0) {
                    Optional<Join<E, ?>> joinPai = root.getJoins().stream()
                            .filter(j -> paths[0].equals(j.getAttribute().getName()) && j.getJoinType() == type)
                            .findFirst();

                    if (joinPai.isPresent()) {
                        join = joinPai.get();
                        continue;
                    }
                }

                join = join.join(path, type);
            }
        } else {
            root.join(attribute, type);
        }
    }

    @Override
    public TupleQuery<E> join(String attribute, String alias) {
        if (attribute.indexOf('.') != -1) {
            String[] parts = attribute.split("\\.");
            String joinClass = parts[0];
            String joinAttr = parts[1];
            for (int i =0; i < this.joins.size(); i++) {
                if (this.joins.get(i).getAlias().equalsIgnoreCase(joinClass)) {
                    Join novo = this.joins.get(i).join(joinAttr);
                    novo.alias(alias);
                    this.joins.add(novo);
                    break;
                }
            }
        } else {
            Join j = root.join(attribute);
            j.alias(alias);
            this.joins.add(j);
        }
        return this;
    }

    @Override
    public TupleQuery<E> leftJoinFetch(String attribute) {
        root.fetch(attribute, JoinType.LEFT);
        return this;
    }

    public TupleQuery<E> rightJoinFetch(String attribute) {
        root.fetch(attribute, JoinType.RIGHT);
        return this;
    }

    @Override
    public TupleQuery<E> addOrderBy(String type, String path) {
        if (type != null && path != null){
            if ("asc".equalsIgnoreCase(type)){
                this.addAscendingOrderBy(path);
            }
            else{
                this.addDescendingOrderBy(path);
            }
        }
        return this;
    }

    @Override
    public TupleQuery<E> addAscendingOrderBy(String path) {
        if (path != null) {
            orders.add(criteriaBuilder.asc(toJpaPath(path)));
        }
        return this;
    }

    @Override
    public TupleQuery<E> addDescendingOrderBy(String path) {
        if (path != null) {
            orders.add(criteriaBuilder.desc(toJpaPath(path)));
        }
        return this;
    }

    public TupleQuery<E> addDescendingOrderByCoalesceToField(String path, String coalesceToPath) {
        if (path != null) {
            orders.add(criteriaBuilder.desc(criteriaBuilder.coalesce(toJpaPath(path), toJpaPath(coalesceToPath))));
        }
        return this;
    }

    public TupleQuery<E> addDescendingOrderBy(String path, Object coalesceValue) {
        if (path != null) {
            orders.add(criteriaBuilder.desc(criteriaBuilder.coalesce(toJpaPath(path), coalesceValue)));
        }
        return this;
    }

    public TupleQuery<E> setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    @Override
    public TupleQuery<E> setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    @Override
    public TupleQuery<E> objectEqualsTo(String path, Object value) {
        if (value != null) {
            addEqualPredicate(path, value);
        }
        return this;
    }

    @Override
    public TupleQuery<E> equal(String path, Object value) {
        if (value != null) {
            addEqualPredicate(path, value);
        }
        return this;
    }

    @Override
    public TupleQuery<E> equalIgnoreCase(String path, String value) {
        if (value != null) {
            addPredicate(criteriaBuilder.equal(criteriaBuilder.upper(toJpaPath(path)), value.toUpperCase()));
        }
        return this;
    }

    @Override
    public TupleQuery<E> equal(String path, Calendar value, TemporalType temporalType) {
        if (value != null) {
            addEqualPredicate(path, value.getTime(), temporalType);
        }
        return this;
    }

    @Override
    public TupleQuery<E> equal(String path, java.util.Date value, TemporalType temporalType) {
        if (value != null) {
            addEqualPredicate(path, value, temporalType);
        }
        return this;
    }

    public TupleQuery<E> orEqual(Or... ors) {
        if (ors != null && ors.length > 0) {
            //verifica se tem valores
            List<Object> orsWithValues = Arrays.stream(ors).filter(or -> or.getValue() != null)
                    .collect(Collectors.toList());

            if (orsWithValues.isEmpty()){
                return this;
            }

            Predicate orPredicates[] = new Predicate[orsWithValues.size()];
            for ( int i = 0; i< orsWithValues.size(); i++ ){
                Or or = ors[i];
                if (or.getValue() != null) {
                    orPredicates[i] = equalPredicate(or.getPath(), or.getValue());
                }
            }
            addPredicate( criteriaBuilder.or(orPredicates) );
        }
        return this;
    }

    @Override
    public TupleQuery<E> notEqual(String path, Object value) {
        if (value != null) {
            addNotEqualPredicate(path, value);
        }
        return this;
    }

    @Override
    public TupleQuery<E> isEmpty(String path, Boolean executeFilter) {
        if (executeFilter != null && executeFilter) {
            addEmptyPredicate(path);
        }
        return this;
    }

    @Override
    public TupleQuery<E> isNullOrNotNull(String path, Boolean value) {
        if (value != null) {
            if (value) {
                addIsNullPredicate(path);
            } else {
                addIsNotNullPredicate(path);
            }
        }
        return this;
    }

    @Override
    public TupleQuery<E> isNotNullOrNull(String path, Boolean value) {
        if (value != null) {
            if (value) {
                addIsNotNullPredicate(path);
            } else {
                addIsNullPredicate(path);
            }
        }
        return this;
    }

    @Override
    public TupleQuery<E> isNull(String path) {
        addIsNullPredicate(path);
        return this;
    }

    @Override
    public TupleQuery<E> isNull(String path, Boolean apply) {
        if (apply != null && apply) {
            addIsNullPredicate(path);
        }
        return this;
    }

    @Override
    public TupleQuery<E> isNotNull(String path, Boolean apply) {
        if (apply != null && apply) {
            addIsNotNullPredicate(path);
        }
        return this;
    }

    @Override
    public TupleQuery<E> isMemberOf(String path, Object value) {
        if (value != null ) {
            addIsMemberOfPredicate(path, value);
        }
        return this;
    }

    @Override
    public TupleQuery<E> isNotEmpty(String path, Boolean executeFilter) {
        if (executeFilter != null && executeFilter) {
            addNotEmptyPredicate(path);
        }
        return this;
    }

    @Override
    public Optional<Predicate> objectEqualsToPredicate(String path, Object value) {
        if (value != null) {
            return Optional.of(equalPredicate(path, value));
        }
        return Optional.empty();
    }

    @Override
    public TupleQuery<E> like(String path, String value) {
        if (value != null && !value.trim().isEmpty()) {
            addPredicate(criteriaBuilder.like(toJpaPath(path), '%' + value + '%'));
        }
        return this;
    }

    @Override
    public TupleQuery<E> likeStart(String path, String value) {
        if (value != null && !value.trim().isEmpty()) {
            addPredicate(criteriaBuilder.like(toJpaPath(path), value + '%'));
        }
        return this;
    }

    @Override
    public TupleQuery<E> likeIgnoreCase(String path, String value) {
        if (value != null && !value.trim().isEmpty()) {
            addPredicate(criteriaBuilder.like(criteriaBuilder.upper( toJpaPath(path) ), '%' + value.toUpperCase() + '%'));
        }
        return this;
    }

    @Override
    public TupleQuery<E> likeIgnoreCaseWords(String path, String value) {
        if (value != null && !value.trim().isEmpty()) {
            String filtro = value.toUpperCase().replaceAll(" ", "%");
            addPredicate(criteriaBuilder.like(criteriaBuilder.upper( toJpaPath(path) ), '%' + filtro + '%'));
        }
        return this;
    }

    public TupleQuery<E> addOr(Predicate ... predicates) {
        if (predicates.length > 1) {
            addPredicate(this.criteriaBuilder.or(predicates));
        } else if (predicates.length == 1) {
            addPredicate(predicates[0]);
        }
        return this;
    }

    @Override
    public TupleQuery<E> stringEqualsTo(String path, String value) {
        if (value != null && !value.trim().isEmpty()) {
            addEqualPredicate(path, value);
        }
        return this;
    }

    @Override
    public TupleQuery<E> lessThanOrEqualsTo(String path, java.util.Date data, Boolean apply) {
        if (Objects.nonNull(data) && Objects.nonNull(apply) && apply) {
            addPredicate(criteriaBuilder.lessThanOrEqualTo(toJpaPath(path), data));
        }
        return this;
    }

    @Override
    public TupleQuery<E> greaterThanOrEqualsTo(String path, java.util.Date data, Boolean apply) {
        if (Objects.nonNull(data) && Objects.nonNull(apply) && apply) {
            addPredicate(criteriaBuilder.lessThanOrEqualTo(toJpaPath(path), data));
        }
        return this;
    }

    @Override
    public TupleQuery<E> greaterThanOrEqualsTo(String path, Comparable comparable) {
        if (Objects.nonNull(comparable)) {
            addPredicate(criteriaBuilder.greaterThanOrEqualTo(toJpaPath(path), comparable));
        }
        return this;
    }

    @Override
    public TupleQuery<E> lessThanOrEqualsTo(String path, Comparable comparable) {
        if (Objects.nonNull(comparable)) {
            addPredicate(criteriaBuilder.lessThanOrEqualTo(toJpaPath(path), comparable));
        }
        return this;
    }

    @Override
    public TupleQuery<E> between(String path, Date firstDate, Date secondDate) {
        if (Objects.nonNull(firstDate) && Objects.nonNull(secondDate)) {
            addPredicate(criteriaBuilder.between(toJpaPath(path), firstDate, secondDate));
        } else if (Objects.nonNull(firstDate)) {
            addPredicate(criteriaBuilder.greaterThanOrEqualTo(toJpaPath(path), firstDate));
        } else if (Objects.nonNull(secondDate)){
            addPredicate(criteriaBuilder.lessThanOrEqualTo(toJpaPath(path), secondDate));
        }
        return this;
    }

    @Override
    public TupleQuery<E> between(String path, ZonedDateTime firstDate, ZonedDateTime secondDate) {
        if (Objects.nonNull(firstDate) && Objects.nonNull(secondDate)) {
            addPredicate(criteriaBuilder.between(toJpaPath(path), firstDate, secondDate));
        } else if (Objects.nonNull(firstDate)) {
            addPredicate(criteriaBuilder.greaterThanOrEqualTo(toJpaPath(path), firstDate));
        } else if (Objects.nonNull(secondDate)){
            addPredicate(criteriaBuilder.lessThanOrEqualTo(toJpaPath(path), secondDate));
        }
        return this;
    }

    @Override
    public TupleQuery<E> between(String path, java.util.Date firstDate, java.util.Date secondDate) {
        if (Objects.nonNull(firstDate) && Objects.nonNull(secondDate)) {
            addPredicate(criteriaBuilder.between(toJpaPath(path), new Date( firstDate.getTime() ), new Date( secondDate.getTime() )));
        } else if (Objects.nonNull(firstDate)) {
            addPredicate(criteriaBuilder.greaterThanOrEqualTo(toJpaPath(path), firstDate));
        } else if (Objects.nonNull(secondDate)){
            addPredicate(criteriaBuilder.lessThanOrEqualTo(toJpaPath(path), secondDate));
        }
        return this;
    }

    @Override
    public TupleQuery<E> in(String path, Collection<?> collection) {
        if (collection != null && !collection.isEmpty()) {
            addPredicate(criteriaBuilder.in(toJpaPath(path)).value(collection));
        }
        return this;
    }

    // Usado para simplificar os casos onde a lista pode estar vazia,
    // e isso irá significar que a query não irá retornar nenhum resultado.
    public TupleQuery<E> in(String path, Collection<?> collection, boolean incluirSeVazio) {
        if (collection != null && (!collection.isEmpty() || incluirSeVazio)) {
            addPredicate(criteriaBuilder.in(toJpaPath(path)).value(collection));
        }
        return this;
    }

    @Override
    public TupleQuery<E> notIn(String path, Collection<?> collection) {
        if (collection != null && !collection.isEmpty()) {
            addPredicate(criteriaBuilder.not(criteriaBuilder.in(toJpaPath(path)).value(collection)));
        }
        return this;
    }

    public TupleQuery<E> groupBy(String ... groupBy) {
        this.groupByPart.addAll( Arrays.asList(groupBy) );
        return this;
    }

    @Override
    public TupleQuery<E> distinct(Boolean value) {
        if (value != null) {
            this.criteriaQuery.distinct(value);
        }
        return this;
    }

    @Override
    public TupleQuery<E> beginOr() {
        // TODO: talvez suportar multiplos or's, um dentro do outro!?
        if (this.insideOr) throw new IllegalStateException("beginOr já foi chamado e não foi finalizado.");
        this.insideOr = true;
        this.orPredicates.clear();
        return this;
    }

    @Override
    public TupleQuery<E> endOr() {
        if (!this.insideOr) throw new IllegalStateException("beginOr não foi chamado.");
        this.insideOr = false;
        if (!this.orPredicates.isEmpty()) {
            this.predicates.add(criteriaBuilder.or(this.orPredicates.toArray(new Predicate[0])));
            this.orPredicates.clear();
        }
        return this;
    }

    private void addEqualPredicate(String path, Object value) {
        addPredicate(equalPredicate(path, value));
    }

    private void addEqualPredicate(String path, java.util.Date value, TemporalType temporalType) {
        addPredicate(equalPredicate(path, value, temporalType));
    }

    private void addNotEqualPredicate(String path, Object value) {
        addPredicate(notEqualPredicate(path, value));
    }

    private void addNotEmptyPredicate(String path) {
        addPredicate(notEmptyPredicate(path));
    }

    private void addEmptyPredicate(String path) {
        addPredicate(emptyPredicate(path));
    }

    private void addIsNullPredicate(String path) {
        addPredicate(nullPredicate(path));
    }

    private void addIsNotNullPredicate(String path) {
        addPredicate(notNullPredicate(path));
    }

    private void addIsMemberOfPredicate(String path, Object value) {
        addPredicate(isMemberOfPredicate(path, value));
    }

    public Predicate inPredicate(String path, Collection<?> collection) {
        return criteriaBuilder.in(toJpaPath(path)).value(collection);
    }

    public Predicate equalPredicate(String path, Object value) {
        return criteriaBuilder.equal(toJpaPath(path), value);
    }

    public Predicate equalPredicate(String path, java.util.Date value, TemporalType temporalType) {
        if (temporalType == TemporalType.DATE){
            Expression<Date> function = criteriaBuilder.function("date", Date.class, toJpaPath(path));
            return criteriaBuilder.equal(function, value);
        }
        else {
            return criteriaBuilder.equal(toJpaPath(path), value);
        }
    }

    public Predicate notEqualPredicate(String path, Object value) {
        return criteriaBuilder.notEqual(toJpaPath(path), value);
    }

    public Predicate notEmptyPredicate(String path) {
        return criteriaBuilder.isNotEmpty(toJpaPath(path));
    }

    public Predicate emptyPredicate(String path) {
        return criteriaBuilder.isEmpty(toJpaPath(path));
    }

    public Predicate notNullPredicate(String path) {
        return criteriaBuilder.isNotNull(toJpaPath(path));
    }

    public Predicate nullPredicate(String path) {
        return criteriaBuilder.isNull(toJpaPath(path));
    }

    public Predicate isMemberOfPredicate(String path, Object value) {
        return criteriaBuilder.isMember(value, toJpaPath(path));
    }

    private <T> Path<T> toJpaPath(String stringPath) {
        // Remove alias
        if (stringPath.contains(" as")) {
            stringPath = stringPath.substring(0, stringPath.indexOf(" as"));
        }

        String[] pathParts = stringPath.split("\\.");
        assert pathParts != null && pathParts.length > 0 : "Path cannot be empty";

        String rootPath = pathParts[0];
        for (Join<E, ?> join : root.getJoins()) {
            if (rootPath.equals(join.getAlias()) || rootPath.equals(join.getAttribute().getName())) {
                return traversePath(join, pathParts, 1);
            }
        }

        return traversePath(root, pathParts);
    }

    private <T> Path<T> traversePath(Path<?> path, String[] pathParts) {
        return this.traversePath(path, pathParts, 0);
    }

    @SuppressWarnings("unchecked")
    private <T> Path<T> traversePath(Path<?> path, String[] pathParts, int offset) {
        for (int i = offset; i < pathParts.length; i++) {
            String pathPart = pathParts[i];
            path = path.get(pathPart);
        }
        return (Path<T>) path;
    }

    @Override
    public TupleQuery<E> addPredicate(Predicate predicate) {
        if (insideOr) {
            this.orPredicates.add(predicate);
        } else {
            this.predicates.add(predicate);
        }
        return this;
    }

    public static <T> T createAndPopulateBean( Class<T> clazz, Tuple tuple ) throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        T bean = clazz.newInstance();

        for( TupleElement<?> e: tuple.getElements() ) {
            String name = e.getAlias();
            if( name.contains(" as ") ){
                name = name.substring( name.indexOf(" as") + 4 );
            }
            PropertyUtils.setSimpleProperty(bean, name, tuple.get(e));
        };

        return bean;
    }

    @Override
    public TupleQuery<E> apply(Consumer<BaseQuery<E>> consumer) {
        consumer.accept(this);
        return this;
    }

    @Override
    public <T> TupleQuery<E> apply(BiConsumer<BaseQuery<E>, T> consumer, T obj) {
        consumer.accept(this, obj);
        return this;
    }

    @SuppressWarnings("unchecked")
    public CriteriaQuery<Tuple> getCriteriaQuery() {
        return criteriaQuery;
    }

    public CriteriaBuilder getCriteriaBuilder() {
        return criteriaBuilder;
    }

    public Root<E> getRoot() {
        return root;
    }

    @Override
    public boolean isCountQuery() {
        return false;
    }
}