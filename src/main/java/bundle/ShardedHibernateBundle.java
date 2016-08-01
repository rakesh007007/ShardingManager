package bundle;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import configuration.pojo.DatabaseShard;
import configuration.pojo.Partition;
import configuration.pojo.PartitionBounds;
import configuration.pojo.Shard;
import interceptors.UnitOfWorkInterceptor;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.SessionFactoryHealthCheck;
import io.dropwizard.hibernate.UnitOfWork;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.aopalliance.intercept.MethodInterceptor;
import org.hibernate.SessionFactory;
import partition.manager.HorizontalPartitionManager;
import partition.strategy.PartitionStrategy;
import partition.strategy.StringBoundedPartitionStrategy;
import sessionFactory.SessionFactoryFactory;
import shardProvider.ShardingContextProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;

public abstract class ShardedHibernateBundle<T extends Configuration>
        implements ConfiguredBundle<T> {
    private static final String DEFAULT_NAME = "hibernate";

    Map<String, SessionFactory> shardedHibernateBundle = new HashMap<>();

    private Injector baseInjector;

    private final ImmutableList<Class<?>> entities;
    private final SessionFactoryFactory sessionFactoryFactory;

    protected ShardedHibernateBundle(Class<?> entity, Class<?>... entities) {
        this(ImmutableList.<Class<?>>builder().add(entity).add(entities)
                .build(), new SessionFactoryFactory());
    }

    protected ShardedHibernateBundle(ImmutableList<Class<?>> entities,
                                     SessionFactoryFactory sessionFactoryFactory) {
        this.entities = entities;
        this.sessionFactoryFactory = sessionFactoryFactory;
    }

    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapper().registerModule(createHibernate4Module());
    }

    /**
     * Override to configure the {@link Hibernate4Module}.
     */
    protected Hibernate4Module createHibernate4Module() {
        return new Hibernate4Module();
    }

    /**
     * Override to configure the name of the bundle (It's used for the bundle
     * health check and database pool metrics)
     */
    protected String name() {
        return DEFAULT_NAME;
    }

    public abstract List<DatabaseShard> getDataSourceConfiguration(
            T configuration);

    @Override
    public final void run(T configuration, Environment environment)
            throws Exception {

        final List<DatabaseShard> databaseShards = getDataSourceConfiguration(configuration);

        for (DatabaseShard shard : databaseShards) {
            final DataSourceFactory dbConfig = shard.getDatabase();
            String displayName = name() + "_" + shard.getShardId();
            SessionFactory sessionFactory = sessionFactoryFactory.build(
                    environment, dbConfig, entities, displayName);
            // We will need to register annotations for @Transactions later.
            shardedHibernateBundle.put(shard.getShardId(), sessionFactory);
            environment.healthChecks().register(
                    displayName,
                    new SessionFactoryHealthCheck(environment
                            .getHealthCheckExecutorService(), dbConfig
                            .getValidationQueryTimeout()
                            .or(Duration.seconds(5)), sessionFactory, dbConfig
                            .getValidationQuery()));

        }

        final PartitionBounds partitionBounds = getPartitionBounds(configuration);
        final HorizontalPartitionManager partitionManager = new HorizontalPartitionManager(getDataSourceConfiguration(configuration));
        final ShardingContextProvider shardingContextProvider = new ShardingContextProvider();

        for (Partition partition : getPartitions(configuration)) {
            partitionManager.addPartition(partition);
            Injector injector = Guice.createInjector(getBasicShardedModule(configuration,partition.getShardId()), getShardDependentModule(configuration, partition.getShardId()));
            shardingContextProvider.addContext(partition.getShardId(), injector);
        }

        baseInjector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
        bind(ShardingContextProvider.class).toInstance(shardingContextProvider);
        bind(new TypeLiteral<PartitionStrategy<String>>() {
        }).toInstance(new StringBoundedPartitionStrategy(partitionBounds.getLowerBound(), partitionBounds.getUpperBound()));
        bind(HorizontalPartitionManager.class).toInstance(partitionManager);

            }
        }, getShardInDependentModule(configuration, environment));
    }

    private AbstractModule getBasicShardedModule(final T configuration, final String shardId) {
      return new AbstractModule() {
            @Override
            protected void configure() {
                requestInjection(configuration);
                bind(Shard.class).toInstance(new Shard(shardId));
                bind(SessionFactory.class).toInstance(getSessionFactory(shardId));
                MethodInterceptor unitOfWorkInterceptor = new UnitOfWorkInterceptor();
                requestInjection(unitOfWorkInterceptor);
                bindInterceptor(any(), annotatedWith(UnitOfWork.class), unitOfWorkInterceptor);
            }
        };
    }

    public Injector getBaseInjector () {
        return baseInjector;
    }

    protected abstract AbstractModule getShardInDependentModule(T configuration, Environment environment);

    protected abstract AbstractModule getShardDependentModule(T configuration, String shardId);

    protected abstract PartitionBounds getPartitionBounds(T configuration);

    protected abstract List<Partition> getPartitions(T configuration);


    public SessionFactory getSessionFactory(String shardId) {
        return shardedHibernateBundle.get(shardId);
    }

    protected void configure(org.hibernate.cfg.Configuration configuration) {
    }
}
