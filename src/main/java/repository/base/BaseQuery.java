package repository.base;

import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import jakarta.persistence.TemporalType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public interface BaseQuery<E> {

	List<?> list();

	BaseQuery<E> join(String attribute);

	BaseQuery<E> join(String attribute, String alias);

	BaseQuery<E> innerJoinFetch(String attribute);

	BaseQuery<E> leftJoinFetch(String attribute);

	BaseQuery<E> leftJoin(String attribute);

	BaseQuery<E> addOrderBy(String type, String path);

	BaseQuery<E> addAscendingOrderBy(String path);

	BaseQuery<E> addDescendingOrderBy(String path);

	BaseQuery<E> setFirstResult(Integer firstResult);

	BaseQuery<E> setMaxResults(Integer maxResults);

	BaseQuery<E> objectEqualsTo(String path, Object value);

	BaseQuery<E> equal(String path, Object value);

	BaseQuery<E> equalIgnoreCase(String path, String value);

	BaseQuery<E> equal(String path, Calendar value, TemporalType temporalType);

	BaseQuery<E> equal(String path, java.util.Date value, TemporalType temporalType);

	BaseQuery<E> notEqual(String path, Object value);

	BaseQuery<E> isEmpty(String path, Boolean executeFilter);

	BaseQuery<E> isNotEmpty(String path, Boolean executeFilter);

	BaseQuery<E> isMemberOf(String path, Object value);

	BaseQuery<E> isNull(String path);

	BaseQuery<E> isNull(String path, Boolean apply);

	BaseQuery<E> isNullOrNotNull(String path, Boolean value);

	BaseQuery<E>  isNotNullOrNull(String path, Boolean value);

	BaseQuery<E> isNotNull(String path, Boolean apply);

	Optional<Predicate> objectEqualsToPredicate(String path, Object value);

	BaseQuery<E> likeStart(String path, String value);

	BaseQuery<E> like(String path, String value);

	BaseQuery<E> likeIgnoreCaseWords(String path, String value);

	BaseQuery<E> likeIgnoreCase(String path, String value);

	BaseQuery<E> stringEqualsTo(String path, String value);

	BaseQuery<E> lessThanOrEqualsTo(String path, java.util.Date data, Boolean apply);

	BaseQuery<E> greaterThanOrEqualsTo(String path, java.util.Date data, Boolean apply);

	BaseQuery<E> greaterThanOrEqualsTo(String path, Comparable<?> comparable);

	BaseQuery<E> lessThanOrEqualsTo(String path, Comparable<?> comparable);

	BaseQuery<E> between(String path, Date firstDate, Date secondDate);

	BaseQuery<E> between(String path, ZonedDateTime firstDate, ZonedDateTime secondDate);

	BaseQuery<E> between(String path, java.util.Date firstDate, java.util.Date secondDate);

	BaseQuery<E> in(String path, Collection<?> collection, boolean incluirSeVazio);

	BaseQuery<E> in(String path, Collection<?> collection);

	BaseQuery<E> notIn(String path, Collection<?> collection);

	BaseQuery<E> apply(Consumer<BaseQuery<E>> consumer);

	BaseQuery<E> distinct(Boolean value);

	BaseQuery<E> addPredicate(Predicate predicate);

	boolean isCountQuery();

	/**
	 * Inicia um "bloco" OR.
	 */
	BaseQuery<E> beginOr();

	/**
	 * Termina um "bloco" OR.
	 */
	BaseQuery<E> endOr();

	<T> BaseQuery<E> apply(BiConsumer<BaseQuery<E>, T> consumer, T obj);

	<T> CriteriaQuery<T> getCriteriaQuery();

	CriteriaBuilder getCriteriaBuilder();

	Root<E> getRoot();
}