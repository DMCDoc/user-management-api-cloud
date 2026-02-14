HTTP Request
 → JWT Filter
    → TenantContext.setTenantId()
 → TenantHibernateFilter
    → Hibernate filter ON
 → Controller
 → Service
 → Repository
 → SQL (tenant_id = ?)
 → finally → TenantContext.clear()
