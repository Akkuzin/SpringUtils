package aaa.utils.spring.metrics;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hibernate.SessionFactory;
import org.hibernate.stat.CacheRegionStatistics;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

@Component
@ManagedResource("Statistics:name=HibernateStatistics")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(onConstructor = @__({@Autowired}))
public class HibernateStatistics {

  SessionFactory sessionFactory;

  @ManagedOperation
  public void clear() {
    sessionFactory.getStatistics().clear();
  }

  @ManagedOperation
  public EntityStatistics getEntityStatistics(String entityName) {
    return sessionFactory.getStatistics().getEntityStatistics(entityName);
  }

  @ManagedOperation
  public CollectionStatistics getCollectionStatistics(String role) {
    return sessionFactory.getStatistics().getCollectionStatistics(role);
  }

  @ManagedOperation
  public CacheRegionStatistics getCacheRegionStatistics(String regionName) {
    return sessionFactory.getStatistics().getCacheRegionStatistics(regionName);
  }

  @ManagedOperation
  public CacheRegionStatistics getQueryRegionStatistics(String regionName) {
    return sessionFactory.getStatistics().getQueryRegionStatistics(regionName);
  }

  @ManagedOperation
  public CacheRegionStatistics getDomainDataRegionStatistics(String regionName) {
    return sessionFactory.getStatistics().getDomainDataRegionStatistics(regionName);
  }

  @ManagedOperation
  public QueryStatistics getQueryStatistics(String hql) {
    return sessionFactory.getStatistics().getQueryStatistics(hql);
  }

  @ManagedAttribute
  public long getEntityDeleteCount() {
    return sessionFactory.getStatistics().getEntityDeleteCount();
  }

  @ManagedAttribute
  public long getEntityInsertCount() {
    return sessionFactory.getStatistics().getEntityInsertCount();
  }

  @ManagedAttribute
  public long getEntityLoadCount() {
    return sessionFactory.getStatistics().getEntityLoadCount();
  }

  @ManagedAttribute
  public long getEntityFetchCount() {
    return sessionFactory.getStatistics().getEntityFetchCount();
  }

  @ManagedAttribute
  public long getEntityUpdateCount() {
    return sessionFactory.getStatistics().getEntityUpdateCount();
  }

  @ManagedAttribute
  public long getQueryExecutionCount() {
    return sessionFactory.getStatistics().getQueryExecutionCount();
  }

  @ManagedAttribute
  public long getQueryCacheHitCount() {
    return sessionFactory.getStatistics().getQueryCacheHitCount();
  }

  @ManagedAttribute
  public long getQueryExecutionMaxTime() {
    return sessionFactory.getStatistics().getQueryExecutionMaxTime();
  }

  @ManagedAttribute
  public long getQueryCacheMissCount() {
    return sessionFactory.getStatistics().getQueryCacheMissCount();
  }

  @ManagedAttribute
  public long getQueryCachePutCount() {
    return sessionFactory.getStatistics().getQueryCachePutCount();
  }

  @ManagedAttribute
  public long getFlushCount() {
    return sessionFactory.getStatistics().getFlushCount();
  }

  @ManagedAttribute
  public long getConnectCount() {
    return sessionFactory.getStatistics().getConnectCount();
  }

  @ManagedAttribute
  public long getSecondLevelCacheHitCount() {
    return sessionFactory.getStatistics().getSecondLevelCacheHitCount();
  }

  @ManagedAttribute
  public long getSecondLevelCacheMissCount() {
    return sessionFactory.getStatistics().getSecondLevelCacheMissCount();
  }

  @ManagedAttribute
  public long getSecondLevelCachePutCount() {
    return sessionFactory.getStatistics().getSecondLevelCachePutCount();
  }

  @ManagedAttribute
  public long getSessionCloseCount() {
    return sessionFactory.getStatistics().getSessionCloseCount();
  }

  @ManagedAttribute
  public long getSessionOpenCount() {
    return sessionFactory.getStatistics().getSessionOpenCount();
  }

  @ManagedAttribute
  public long getCollectionLoadCount() {
    return sessionFactory.getStatistics().getCollectionLoadCount();
  }

  @ManagedAttribute
  public long getCollectionFetchCount() {
    return sessionFactory.getStatistics().getCollectionFetchCount();
  }

  @ManagedAttribute
  public long getCollectionUpdateCount() {
    return sessionFactory.getStatistics().getCollectionUpdateCount();
  }

  @ManagedAttribute
  public long getCollectionRemoveCount() {
    return sessionFactory.getStatistics().getCollectionRemoveCount();
  }

  @ManagedAttribute
  public long getCollectionRecreateCount() {
    return sessionFactory.getStatistics().getCollectionRecreateCount();
  }

  @ManagedAttribute
  public Instant getStart() {
    return sessionFactory.getStatistics().getStart();
  }

  @ManagedAttribute
  public boolean isStatisticsEnabled() {
    return sessionFactory.getStatistics().isStatisticsEnabled();
  }

  @ManagedOperation
  public void setStatisticsEnabled(boolean enable) {
    sessionFactory.getStatistics().setStatisticsEnabled(enable);
  }

  @ManagedOperation
  public void logSummary() {
    sessionFactory.getStatistics().logSummary();
  }

  @ManagedAttribute
  public String[] getCollectionRoleNames() {
    return sessionFactory.getStatistics().getCollectionRoleNames();
  }

  @ManagedAttribute
  public String[] getEntityNames() {
    return sessionFactory.getStatistics().getEntityNames();
  }

  @ManagedAttribute
  public String[] getQueries() {
    return sessionFactory.getStatistics().getQueries();
  }

  @ManagedAttribute
  public String[] getSecondLevelCacheRegionNames() {
    return sessionFactory.getStatistics().getSecondLevelCacheRegionNames();
  }

  @ManagedAttribute
  public long getSuccessfulTransactionCount() {
    return sessionFactory.getStatistics().getSuccessfulTransactionCount();
  }

  @ManagedAttribute
  public long getTransactionCount() {
    return sessionFactory.getStatistics().getTransactionCount();
  }

  @ManagedAttribute
  public long getCloseStatementCount() {
    return sessionFactory.getStatistics().getCloseStatementCount();
  }

  @ManagedAttribute
  public long getPrepareStatementCount() {
    return sessionFactory.getStatistics().getPrepareStatementCount();
  }

  @ManagedAttribute
  public long getOptimisticFailureCount() {
    return sessionFactory.getStatistics().getOptimisticFailureCount();
  }

  @ManagedAttribute
  public String getQueryExecutionMaxTimeQueryString() {
    return sessionFactory.getStatistics().getQueryExecutionMaxTimeQueryString();
  }
}
