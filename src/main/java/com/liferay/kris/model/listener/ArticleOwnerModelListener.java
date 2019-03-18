package com.liferay.kris.model.listener;

import com.liferay.portal.kernel.model.ModelListener;
import com.liferay.portal.kernel.service.ServiceContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.LocalizationUtil;
import com.liferay.portal.kernel.util.PortalUtil;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.liferay.journal.model.JournalArticle;
import com.liferay.journal.service.JournalArticleLocalService;
import com.liferay.portal.kernel.audit.AuditMessage;
import com.liferay.portal.kernel.audit.AuditRouterUtil;
import com.liferay.portal.kernel.exception.ModelListenerException;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.BaseModelListener;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;



/**
 * @author kpatefield
 */
@Component(
	immediate = true,
	service = ModelListener.class
)
public class ArticleOwnerModelListener extends BaseModelListener<JournalArticle> {
	
	@Activate
	public void activate(BundleContext context) {
		String location = context.getBundle().getLocation();
		_log.info("bundle " + location + " activated!!!");
	}

	@Override
	public void onAfterCreate(JournalArticle model) throws ModelListenerException {
				
		//should be put into a OCD OSGI conf
		long systemUserId = 20139;		
		try {
			JournalArticle oldArticle = journalArticleLocalService.getArticle(model.getId());
			
			//should set the resource information here as well if required
			AuditMessage creationAuditMessage = new AuditMessage("Journal Article - Add - " + oldArticle.getArticleId(), oldArticle.getCompanyId(), oldArticle.getUserId(), "User" , oldArticle.getClassName(), Long.toString(oldArticle.getClassPK()));
			creationAuditMessage.setClassName(oldArticle.getClassName());
			creationAuditMessage.setClassPK(oldArticle.getClassPK());
			AuditRouterUtil.route(creationAuditMessage);
			
			//updates the article owner and then persists it
			oldArticle.setUserId(systemUserId);			
			journalArticleLocalService.updateJournalArticle(oldArticle);
			
			//should set the resource information here as well if required
			AuditMessage auditMessage = new AuditMessage("Journal Article - Update - Owner - " + oldArticle.getArticleId(), oldArticle.getCompanyId(), oldArticle.getUserId(), "System" , oldArticle.getClassName(), Long.toString(oldArticle.getClassPK()));
			auditMessage.setClassName(oldArticle.getClassName());
			auditMessage.setClassPK(oldArticle.getClassPK());
			AuditRouterUtil.route(auditMessage);
			
			_log.info("Change JournalArticle content owner - onAfterCreate - check audit logs for details");
			
			/*
			
			//this code is for doing the update the long way round which may or may not create a new version
			//depending on what you do.
			
			Map<Locale, String> titleMap = new HashMap<Locale,String>();
			Locale defaultLocale = PortalUtil.getSiteDefaultLocale(oldArticle.getGroupId());
			titleMap.put(defaultLocale, oldArticle.getUrlTitle());
			ServiceContext serviceContext = new ServiceContext();
			serviceContext.setScopeGroupId(oldArticle.getGroupId());
			serviceContext.setUserId(userId);
			Date createdDate = oldArticle.getCreateDate();
			int displayDateMonth = createdDate.getMonth();
			int displayDateDay = createdDate.getDay();
			int displayDateYear = createdDate.getYear();
			int displayDateHour = createdDate.getHours();
			int displayDateMinute = createdDate.getMinutes();
			int expirationDateMonth = 0;
			int expirationDateDay = 0;
			int expirationDateYear = 0;
			int expirationDateHour = 0;
			int expirationDateMinute = 0;
			boolean neverExpire = true;
			int reviewDateMonth = 0;
			int reviewDateDay = 0;
			int reviewDateYear = 0;
			int reviewDateHour = 0;
			int reviewDateMinute = 0;
			boolean neverReview = true;
			boolean indexable = true;
			boolean smallImage = false;
			String smallImageURL = "";			
			File smallImageFile = null;
			double version = oldArticle.getVersion() + 0.1;
			String newContent = oldArticle.getContent().replace("Blah", "Blah &nbsp;");	 
		 	journalArticleLocalService.updateArticle(userId, 
					oldArticle.getGroupId(), oldArticle.getFolderId(), 
					oldArticle.getArticleId(), oldArticle.getVersion(), titleMap, oldArticle.getDescriptionMap(), 
					newContent, 
					null, oldArticle.getDDMTemplateKey(), oldArticle.getLayoutUuid(), 
					displayDateMonth, displayDateDay, displayDateYear, 
					displayDateHour, displayDateMinute, expirationDateMonth, 
					expirationDateDay, expirationDateYear, expirationDateHour, 
					expirationDateMinute, neverExpire, reviewDateMonth, 
					reviewDateDay, reviewDateYear, 
					reviewDateHour, reviewDateMinute, 
					neverReview, indexable, smallImage, 
					smallImageURL, smallImageFile,
					null, null, serviceContext);	
			
			 */
			
		} catch (PortalException e) {
			
			e.printStackTrace();
		}
		
		super.onAfterCreate(model);
	}	
	
	private static final Log _log = LogFactoryUtil.getLog(ArticleOwnerModelListener.class);
	
	//wires in the JournalArticleLocalService
	@Reference
	JournalArticleLocalService journalArticleLocalService;
	
}