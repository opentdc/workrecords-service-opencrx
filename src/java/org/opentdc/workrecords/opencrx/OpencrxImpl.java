package org.opentdc.workrecords.opencrx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.opencrx.kernel.account1.jmi1.Account;
import org.opencrx.kernel.activity1.cci2.ActivityQuery;
import org.opencrx.kernel.activity1.cci2.ActivityTrackerQuery;
import org.opencrx.kernel.activity1.cci2.ActivityTypeQuery;
import org.opencrx.kernel.activity1.jmi1.AccountAssignmentActivityGroup;
import org.opencrx.kernel.activity1.jmi1.Activity;
import org.opencrx.kernel.activity1.jmi1.ActivityCreator;
import org.opencrx.kernel.activity1.jmi1.ActivityTracker;
import org.opencrx.kernel.activity1.jmi1.ActivityType;
import org.opencrx.kernel.activity1.jmi1.NewActivityParams;
import org.opencrx.kernel.activity1.jmi1.NewActivityResult;
import org.opencrx.kernel.utils.Utils;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.opentdc.exception.DuplicateException;
import org.opentdc.exception.NotFoundException;
import org.opentdc.exception.NotImplementedException;
import org.opentdc.exception.ValidationException;
import org.opentdc.workrecords.StorageProvider;
import org.opentdc.workrecords.WorkRecordModel;
import org.w3c.spi2.Datatypes;
import org.w3c.spi2.Structures;

public class OpencrxImpl extends StorageProvider {
	public static final String XRI_ACTIVITY_SEGMENT = "xri://@openmdx*org.opencrx.kernel.activity1";
	public static final String XRI_ACCOUNT_SEGMENT = "xri://@openmdx*org.opencrx.kernel.account1";
	public static final short ACTIVITY_GROUP_TYPE_PROJECT = 40;
	public static final short ACCOUNT_ROLE_CUSTOMER = 100;
	public static final short ACTIVITY_CLASS_INCIDENT = 2;
	public static final short ICAL_TYPE_NA = 0;
	public static final short ICAL_CLASS_NA = 0;
	public static final short ICAL_TYPE_VEVENT = 1;

	private static PersistenceManagerFactory pmf = null;
	private static String providerName = null;
	private static String segmentName = null;
	private static org.opencrx.kernel.activity1.jmi1.Segment activitySegment = null;
	private static String url = null;
	private static String userName = null;
	private static String password = null;
	private static String mimeType = null;

	// instance variables

	public OpencrxImpl(ServletContext context) {
		logger.info("> OpencrxImpl()");

		super.initStorageProvider();

		if (url == null) {
			url = context.getInitParameter("backend.url");
		}
		if (userName == null) {
			userName = context.getInitParameter("backend.userName");
		}
		if (password == null) {
			password = context.getInitParameter("backend.password");
		}
		if (mimeType == null) {
			mimeType = context.getInitParameter("backend.mimeType");
		}
		if (providerName == null) {
			providerName = context.getInitParameter("backend.providerName");
		}
		if (segmentName == null) {
			segmentName = context.getInitParameter("backend.segmentName");
		}
		if (activitySegment == null) {
			activitySegment = getActivitySegment(getPersistenceManager());
		}

		logger.info("OpencrxImpl() initialized");
	}

	/******************************** workrecord *****************************************/
	/**
	 * List all workrecords.
	 * 
	 * @return a list of all workrecords.
	 */
	@Override
	public ArrayList<WorkRecordModel> listWorkRecords() {
		// TODO: implement listWorkRecords
		logger.info("listWorkRecords() -> " + countWorkRecords() + " workrecords");
		throw new NotImplementedException("listWorkRecords is not yet implemented");
	}

	/**
	 * Create a new WorkRecord.
	 * 
	 * @param workrecord
	 * @return the newly created workrecord (can be different than workrecord param)
	 * @throws DuplicateException
	 *             if a workrecord with the same ID already exists.
	 */
	@Override
	public WorkRecordModel createWorkRecord(WorkRecordModel workrecord) throws DuplicateException {
		if (readWorkRecord(workrecord.getId()) != null) {
			// object with same ID exists already
			throw new DuplicateException();
		}
		// TODO: implement createWorkRecord
		logger.info("createWorkRecord() -> " + countWorkRecords() + " workrecords");
		throw new NotImplementedException(
			"method createWorkRecord is not yet implemented for opencrx storage");
		// logger.info("createWorkRecord() -> " + workrecord);
	}

	/**
	 * Find a WorkRecord by ID.
	 * 
	 * @param id
	 *            the WorkRecord ID
	 * @return the WorkRecord
	 * @throws NotFoundException
	 *             if there exists no WorkRecord with this ID
	 */
	@Override
	public WorkRecordModel readWorkRecord(String xri) throws NotFoundException {
		WorkRecordModel _workrecord = null;
		// TODO: implement readWorkRecord()
		throw new NotImplementedException(
			"method readWorkRecord() is not yet implemented for opencrx storage");
		// logger.info("readWorkRecord(" + xri + ") -> " + _workrecord);
	}

	@Override
	public WorkRecordModel updateWorkRecord(WorkRecordModel workrecord) throws NotFoundException {
		WorkRecordModel _workrecord = null;
		// TODO implement updateWorkRecord()
		throw new NotImplementedException(
				"method updateWorkRecord() is not yet implemented for opencrx storage.");
	}

	@Override
	public void deleteWorkRecord(String id) throws NotFoundException {
		// TODO implement deleteWorkRecord()
		throw new NotImplementedException(
				"method deleteWorkRecord() is not yet implemented for opencrx storage.");
	}

	@Override
	public int countWorkRecords() {
		int _count = -1;
		// TODO: implement countWorkRecords()
		throw new NotImplementedException(
				"method countWorkRecords() is not yet implemented for opencrx storage.");
		// logger.info("countWorkRecords() = " + _count);
		// return _count;
	}


	/******************************** utility methods *****************************************/
	/**
	 * Get persistence manager for configured user.
	 *
	 * @return the PersistenceManager
	 * @throws ServiceException
	 * @throws NamingException
	 */
	public PersistenceManager getPersistenceManager() {

		if (pmf == null) {
			try {
				pmf = org.opencrx.kernel.utils.Utils
						.getPersistenceManagerFactoryProxy(url, userName,
								password, mimeType);
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (ServiceException e) {
				e.printStackTrace();
			}
		}
		return pmf.getPersistenceManager(userName, null);
	}

	/**
	 * Get activity segment.
	 * 
	 * @param pm
	 * @return
	 */
	public static org.opencrx.kernel.activity1.jmi1.Segment getActivitySegment(
			PersistenceManager pm) {
		return (org.opencrx.kernel.activity1.jmi1.Segment) pm
				.getObjectById(new Path(XRI_ACTIVITY_SEGMENT).getDescendant(
						"provider", providerName, "segment", segmentName));
	}
}
