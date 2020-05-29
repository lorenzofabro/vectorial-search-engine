package commons;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import exceptions.TechnicalException;
import java.util.ArrayList;
import java.util.Properties;
import javax.inject.Inject;
import javax.transaction.Transactional;

/**
 * Clase base para los Daos que utilicen JPA
 *
 * @author Felipe
 *
 * @param <E> Tipo de la entidad asociada
 * @param <K> Tipo de la clave primaria de la entidad asociada
 */
public abstract class DaoEclipseLink<E extends DalEntity, K> implements Dao<E, K> {

    //@Inject
    @PersistenceContext(unitName = "tpmotorbusquedaPU")
    protected EntityManager entityManager;

    private final Class<E> entityClass;

    public DaoEclipseLink(Class<E> entityClass) //
    {
        this.entityClass = entityClass;

    }

    protected Class<E> getEntityClass() {
        return entityClass;
    }

    @Override
    @Transactional
    public E create(E pData) {
        try {
            entityManager.persist(pData);
            entityManager.flush();
        } catch (Exception ex) {
            throw new TechnicalException(ex);
        }

        return pData;
    }

    @Override
    @Transactional
    public ArrayList<E> insertArrayList(ArrayList<E> arrayListData) {
        Properties properties = new Properties();
        properties.put("hibernate.jdbc.batch_size", String.valueOf(arrayListData.size()));
        try {
            for (E data : arrayListData) {
                entityManager.persist(data);
            }
            entityManager.flush();
        } catch (Exception ex) {
            throw new TechnicalException(ex);
        }

        return arrayListData;
    }

    @Override
    @Transactional
    public void update(E pData) {
        try {
            E managed = entityManager.merge(pData);
            entityManager.persist(managed);
            entityManager.flush();
        } catch (Exception ex) {
            throw new TechnicalException(ex);
        }
    }

    @Override
    @Transactional
    public void updateArrayList(ArrayList<E> arrayListData) {
        Properties properties = new Properties();
//        properties.put("hibernate.jdbc.batch_size", arrayListData.size());
        properties.put("hibernate.order_updates", "true");
        properties.put("hibernate.batch_versioned_data", "true");
        try {
            for (E data : arrayListData) {
                E managed = entityManager.merge(data);
                entityManager.persist(managed);
            }
            entityManager.flush();
        } catch (Exception ex) {
            throw new TechnicalException(ex);
        }
    }

    @Override
    @Transactional
    public void delete(K pKey) {
        try {
            entityManager.remove(retrieve(pKey));
            entityManager.flush();
        } catch (Exception ex) {
            throw new TechnicalException(ex);
        }
    }

    @Override
    public E retrieve(K pKey) {
        return entityManager.find(getEntityClass(), pKey);
    }

    @Override
    public List<E> findAll() {
        try {
            String className = getEntityClass().getSimpleName();
            Query query = entityManager.createNamedQuery(className + ".findAll");
            return query.getResultList();
        } catch (Exception ex) {
            throw new TechnicalException(ex);
        }

    }

    public List<E> findByFilter(String filter) {
        try {
            String className = getEntityClass().getSimpleName();
            Query query = entityManager.createNamedQuery(className + ".findByFilter")
                    .setParameter(":filter", filter);

            return query.getResultList();
        } catch (Exception ex) {
            throw new TechnicalException(ex);
        }

    }

}
