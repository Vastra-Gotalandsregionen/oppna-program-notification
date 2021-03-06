/**
 * Copyright 2010 Västra Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 *
 */

package se.vgregion.notifications.aspect;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;

/**
 * Aspect handling caching for various services.
 */
@Aspect
public class NotificationsCacheAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationsCacheAspect.class);

    @Resource(name = "servicesCache")
    private Ehcache cache;

    public void setCache(Ehcache cache) {
        this.cache = cache;
    }

    /**
     * Around advice for caching Alfresco documents.
     *
     * @param joinPoint joinPoint
     * @return a cached or non-cached response
     * @throws Throwable Throwable
     */
    @Around("execution(* se.vgregion.notifications.service.AlfrescoDocumentsService.getRecentlyModified("
            + "java.lang.String,boolean))")
    public Object cacheAlfrescoServicesResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        return handleRequest(joinPoint);
    }

    /**
     * Around advice for caching USD issues.
     *
     * @param joinPoint joinPoint
     * @return a cached or non-cached response
     * @throws Throwable Throwable
     */
    @Around("execution(* se.vgregion.notifications.service.UsdIssuesService.getUsdIssues("
            + "java.lang.String,boolean))")
    public Object cacheUsdIssuesServicesResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        return handleRequest(joinPoint);
    }

    /**
     * Around advice for caching Raindance invoices.
     *
     * @param joinPoint joinPoint
     * @return a cached or non-cached response
     * @throws Throwable Throwable
     */
    @Around("execution(* se.vgregion.notifications.service.RaindanceInvoiceService.getInvoices("
            + "java.lang.String,boolean))")
    public Object cacheRaindanceInvoiceServicesResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        return handleRequest(joinPoint);
    }

    /**
     * Around advice for caching social requests.
     *
     * @param joinPoint joinPoint
     * @return a cached or non-cached response
     * @throws Throwable Throwable
     */
    @Around("execution(* se.vgregion.notifications.service.SocialRelationService.getUserRequests("
            + "com.liferay.portal.model.User,boolean))")
    public Object cacheSocialRelationServicesResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        return handleRequest(joinPoint);
    }

    /**
     * Around advice for caching MedControl Cases.
     *
     * @param joinPoint joinPoint
     * @return a cached or non-cached response
     * @throws Throwable Throwable
     */
    @Around("execution(* se.vgregion.notifications.service.MedControlService.listDeviationCases("
            + "java.lang.String,boolean))")
    public Object cacheMedControlServicesResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        return handleRequest(joinPoint);
    }

    private Object handleRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] arguments = joinPoint.getArgs();

        Boolean tryCache = (Boolean) arguments[1];

        int methodCallHash = joinPoint.getSignature().toLongString().hashCode();
        methodCallHash += arguments[0].hashCode();

        if (!tryCache) {
            // Get fresh result
            Object result = joinPoint.proceed();
            // We still cache the result
            cache.put(new Element(methodCallHash, result));
            return result;
        } else {
            Element element = cache.get(methodCallHash);
            if (element != null) {
                LOGGER.debug("Cache element found.");
                return element.getValue();
            } else {
                LOGGER.debug("Cache element not found.");
                Object result = joinPoint.proceed();
                cache.put(new Element(methodCallHash, result));
                return result;
            }
        }
    }
}
