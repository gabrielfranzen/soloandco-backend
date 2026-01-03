package repository.base;

import java.lang.reflect.Type;
import java.util.List;

import jakarta.annotation.PostConstruct;

import java.lang.reflect.ParameterizedType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

public abstract class AbstractCrudRepository<T> {

	protected Class<T> persistentClass;

	@PersistenceContext(unitName = "soloandco")
	protected EntityManager em;

	@PostConstruct
	@SuppressWarnings("unchecked")
	public void init() {
		Type type = getClass().getGenericSuperclass();

		if (type instanceof ParameterizedType) {
			this.persistentClass = (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[0];
		} else if (type instanceof Class) {
			Type genericSuperclass = ((Class<?>) type).getGenericSuperclass();
			if (genericSuperclass instanceof ParameterizedType) {
				this.persistentClass = (Class<T>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0];
			} else {
				throw new IllegalStateException("Unable to determine persistentClass");
			}
		} else {
			throw new IllegalStateException("Unable to determine persistentClass");
		}

		System.out.println(">>> persistentClass set to: " + persistentClass.getName());
	}
	
	protected EntityQuery<T> createEntityQuery() {
		return EntityQuery.create(this.em, this.persistentClass);
	}

	protected EntityQuery<T> createCountQuery() {
		return EntityQuery.createCount(this.em, this.persistentClass);
	}

	protected TupleQuery<T> createTupleQuery() {
		return TupleQuery.create(this.em, this.persistentClass);
	}

	public T consultar(Integer id) {
		return this.em.find(persistentClass, id);
	}

	public List<T> pesquisarTodos() {
		CriteriaBuilder builder = this.em.getCriteriaBuilder();
		CriteriaQuery<T> criteria = builder.createQuery(this.persistentClass);
		Root<T> root = criteria.from(this.persistentClass);
		criteria.select(root);
		return em.createQuery(criteria).getResultList();
	}

	@Transactional
	public T inserir(T entity) {
		this.em.persist(entity);
		return entity;
	}

	@Transactional
	public void atualizar(T entity) {
		this.em.merge(entity);
	}

	@Transactional
	public void remover(T entity) {
		this.em.remove(entity);
	}

	@Transactional
	public void remover(Integer entityId) {
		T entity = this.consultar(entityId);
		if (entity != null) {
			this.remover(entity);
		}
	}

	public EntityManager getEntityManager() {
		return this.em;
	}
}
