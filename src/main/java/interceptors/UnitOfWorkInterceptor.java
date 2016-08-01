package interceptors;

import com.google.inject.Inject;
import io.dropwizard.hibernate.UnitOfWork;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

public class UnitOfWorkInterceptor implements MethodInterceptor {

    @Inject
    private SessionFactory sessionFactory;

    @Override
    public Object invoke(MethodInvocation arg0) throws Throwable {
        Session session = null;
        UnitOfWork unitOfWork = arg0.getMethod()
                .getAnnotation(UnitOfWork.class);
        if (!ManagedSessionContext.hasBind(sessionFactory)) {
            session = openSession(unitOfWork);
        }

        try {
            Object response = arg0.proceed();
            if (session != null) {
                closeSession(session, unitOfWork);
            }
            return response;
        } catch (Throwable t) {
            if (session != null) {
                rollbackSession(session, unitOfWork);
            }
            throw t;
        }

    }

    private Session openSession(UnitOfWork unitOfWork) throws Throwable {
        Session session = this.sessionFactory.openSession();
        try {
            configureSession(session, unitOfWork);
            ManagedSessionContext.bind(session);
            beginTransaction(session, unitOfWork);
        } catch (Throwable th) {
            session.close();
            session = null;
            ManagedSessionContext.unbind(this.sessionFactory);
            throw th;
        }
        return session;
    }

    private void beginTransaction(Session session, UnitOfWork unitOfWork) {
        if (unitOfWork.transactional()) {
            session.beginTransaction();
        }
    }

    private void configureSession(Session session, UnitOfWork unitOfWork) {
        session.setDefaultReadOnly(unitOfWork.readOnly());
        session.setCacheMode(unitOfWork.cacheMode());
        session.setFlushMode(unitOfWork.flushMode());
    }

    private void closeSession(Session session, UnitOfWork unitOfWork) {
        try {
            commitTransaction(session, unitOfWork);
        } catch (Exception e) {
            rollbackTransaction(session, unitOfWork);
            throw new MappableException(e);
        } finally {
            session.close();
            session = null;
            ManagedSessionContext.unbind(this.sessionFactory);
        }
    }

    private void commitTransaction(Session session, UnitOfWork unitOfWork) {
        if (unitOfWork.transactional()) {
            final Transaction txn = session.getTransaction();
            if (txn != null && txn.isActive()) {
                txn.commit();
            }
        }
    }

    private void rollbackSession(Session session, UnitOfWork unitOfWork) {
        try {
            rollbackTransaction(session, unitOfWork);
        } finally {
            session.close();
            session = null;
            ManagedSessionContext.unbind(this.sessionFactory);
        }
    }

    private void rollbackTransaction(Session session, UnitOfWork unitOfWork) {
        if (unitOfWork.transactional()) {
            final Transaction txn = session.getTransaction();
            if (txn != null && txn.isActive()) {
                txn.rollback();
            }
        }
    }

}
