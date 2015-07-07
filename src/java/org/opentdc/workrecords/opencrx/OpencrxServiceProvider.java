/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Arbalo AG
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.opentdc.workrecords.opencrx;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.opencrx.kernel.activity1.cci2.WorkAndExpenseRecordQuery;
import org.opencrx.kernel.activity1.jmi1.Activity;
import org.opencrx.kernel.activity1.jmi1.ActivityAddWorkRecordParams;
import org.opencrx.kernel.activity1.jmi1.ActivityTracker;
import org.opencrx.kernel.activity1.jmi1.AddWorkAndExpenseRecordResult;
import org.opencrx.kernel.activity1.jmi1.Resource;
import org.opencrx.kernel.activity1.jmi1.WorkAndExpenseRecord;
import org.openmdx.base.exception.ServiceException;
import org.openmdx.base.naming.Path;
import org.opentdc.opencrx.AbstractOpencrxServiceProvider;
import org.opentdc.opencrx.ActivitiesHelper;
import org.opentdc.service.exception.DuplicateException;
import org.opentdc.service.exception.InternalServerErrorException;
import org.opentdc.service.exception.NotFoundException;
import org.opentdc.service.exception.ValidationException;
import org.opentdc.workrecords.ServiceProvider;
import org.opentdc.workrecords.WorkRecordModel;
import org.w3c.spi2.Datatypes;
import org.w3c.spi2.Structures;

public class OpencrxServiceProvider extends AbstractOpencrxServiceProvider implements ServiceProvider {
	
	// instance variables
	protected static final Logger logger = Logger.getLogger(OpencrxServiceProvider.class.getName());
	
	public OpencrxServiceProvider(
		ServletContext context,
		String prefix
	) throws ServiceException, NamingException {
		super(context, prefix);
	}

	/**
	 * Map to work record model.
	 * 
	 * @param project
	 * @return
	 */
	protected WorkRecordModel mapToWorkRecord(
		WorkAndExpenseRecord workRecord
	) {
		Activity project = workRecord.getActivity();
		Resource resource = workRecord.getResource();
		ActivityTracker customerProjectGroup = ActivitiesHelper.getCustomerProjectGroup(project);
		WorkRecordModel workRecordModel = new WorkRecordModel();
		workRecordModel.setCreatedAt(workRecord.getCreatedAt());
		workRecordModel.setCreatedBy(workRecord.getCreatedBy().get(0));
		workRecordModel.setModifiedAt(workRecord.getModifiedAt());
		workRecordModel.setModifiedBy(workRecord.getModifiedBy().get(0));
		workRecordModel.setId(this.getWorkRecordId(workRecord));
		workRecordModel.setCompanyId(
			customerProjectGroup == null 
				? null 
				: customerProjectGroup.refGetPath().getLastSegment().toClassicRepresentation()
		);
		workRecordModel.setCompanyTitle(
			customerProjectGroup == null
				? null
				: customerProjectGroup.getName()
		);
		workRecordModel.setProjectId(workRecord.getActivity().refGetPath().getLastSegment().toClassicRepresentation());
		workRecordModel.setProjectTitle(project.getName());
		workRecordModel.setBillable(workRecord.isBillable());
		workRecordModel.setComment(workRecord.getName() == null || workRecord.getName().isEmpty() ? null : workRecord.getName());
		workRecordModel.setStartAt(workRecord.getStartedAt());
		BigDecimal quantity = workRecord.getQuantity();
		workRecordModel.setDurationHours(quantity.intValue());
		workRecordModel.setDurationMinutes(quantity.subtract(new BigDecimal(quantity.toBigInteger())).multiply(new BigDecimal(60.0)).intValue());
		workRecordModel.setRateId(workRecord.getUserString0());
		workRecordModel.setResourceId(resource.refGetPath().getLastSegment().toClassicRepresentation());
		return workRecordModel;
	}

	/**
	 * Get id for given work record.
	 * 
	 * @param workRecord
	 * @return
	 */
	public String getWorkRecordId(
		WorkAndExpenseRecord workRecord
	) {
		Path path = workRecord.refGetPath();
		return path.getSegment(6).toClassicRepresentation() + ":" + path.getSegment(8).toClassicRepresentation() + ":" + path.getSegment(10).toClassicRepresentation();
	}
	
	/**
	 * Get work record by its id.
	 * 
	 * @param activitySegment
	 * @param id
	 * @return
	 */
	protected WorkAndExpenseRecord getWorkRecord(
		org.opencrx.kernel.activity1.jmi1.Segment activitySegment,
		String id
	) {
		PersistenceManager pm = JDOHelper.getPersistenceManager(activitySegment);
		String[] ids = id.split(":");
		if(ids.length != 3) {
			return null;
		} else {
			return (WorkAndExpenseRecord)pm.getObjectById(
				activitySegment.refGetPath().getDescendant("activity", ids[0], "assignedResource", ids[1], "workRecord", ids[2])
			);
		}
	}

	/******************************** workrecord *****************************************/
	/**
	 * List all workrecords.
	 * 
	 * @return a list of all workrecords.
	 */
	@Override
	public List<WorkRecordModel> listWorkRecords(
		String queryType,
		String query,
		int position,
		int size
	) {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.activity1.jmi1.Segment activitySegment = this.getActivitySegment();
		WorkAndExpenseRecordQuery workRecordQuery = (WorkAndExpenseRecordQuery)pm.newQuery(WorkAndExpenseRecord.class);
		workRecordQuery.recordType().equalTo(ActivitiesHelper.WORKRECORD_TYPE_USER_DEFINED);
		workRecordQuery.forAllDisabled().isFalse();
		List<WorkAndExpenseRecord> _workRecords = activitySegment.getWorkReportEntry(workRecordQuery);
		List<WorkRecordModel> workRecords = new ArrayList<WorkRecordModel>();
		int count = 0;
		for(Iterator<WorkAndExpenseRecord> i = _workRecords.listIterator(position); i.hasNext(); ) {
			WorkAndExpenseRecord tracker = i.next();
			workRecords.add(this.mapToWorkRecord(tracker));
			count++;
			if(count >= size) break;
		}
		logger.info("listWorkRecords() -> " + workRecords.size() + " work records");
		return workRecords;		
	}

	/* (non-Javadoc)
	 * @see org.opentdc.workrecords.ServiceProvider#createWorkRecord(org.opentdc.workrecords.WorkRecordModel)
	 */
	@Override
	public WorkRecordModel createWorkRecord(
		WorkRecordModel workrecord
	) throws DuplicateException, ValidationException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.activity1.jmi1.Segment activitySegment = this.getActivitySegment();
		if(workrecord.getId() != null) {
			WorkAndExpenseRecord _workRecord = this.getWorkRecord(activitySegment, workrecord.getId());
			if(_workRecord != null) {
				throw new DuplicateException("work record with ID " + workrecord.getId() + " exists already.");
			} else {
				throw new ValidationException("work record <" + workrecord.getId() + "> contains an ID generated on the client. This is not allowed.");
			}
		}
		// validate mandatory attributes
		if(workrecord.getCompanyId() == null || workrecord.getCompanyId().isEmpty()) {
			throw new ValidationException("workrecord must contain a valid companyId.");
		}
		if(workrecord.getCompanyTitle() == null || workrecord.getCompanyTitle().isEmpty()) {
			throw new ValidationException("workrecord must contain a valid companyTitle.");
		}
		if(workrecord.getProjectId() == null || workrecord.getProjectId().isEmpty()) {
			throw new ValidationException("workrecord must contain a valid projectId.");
		}
		if(workrecord.getProjectTitle() == null || workrecord.getProjectTitle().isEmpty()) {
			throw new ValidationException("workrecord must contain a valid projectTitle.");
		}
		if(workrecord.getResourceId() == null || workrecord.getResourceId().isEmpty()) {
			throw new ValidationException("workrecord must contain a valid resourceId.");
		}
		if(workrecord.getRateId() == null || workrecord.getRateId().isEmpty()) {
			throw new ValidationException("workrecord must contain a valid rateId.");
		}
		if(workrecord.getStartAt() == null) {
			throw new ValidationException("workrecord must contain a valid startAt date.");
		}
		Resource resource = activitySegment.getResource(workrecord.getResourceId());
		Activity project = activitySegment.getActivity(workrecord.getProjectId());
		try {
			pm.currentTransaction().begin();
			ActivityAddWorkRecordParams params = Structures.create(
				ActivityAddWorkRecordParams.class,
				Datatypes.member(ActivityAddWorkRecordParams.Member.name, workrecord.getComment()),
				Datatypes.member(ActivityAddWorkRecordParams.Member.isBillable, workrecord.isBillable()),
				Datatypes.member(ActivityAddWorkRecordParams.Member.startAt, workrecord.getStartAt()),
				Datatypes.member(ActivityAddWorkRecordParams.Member.durationHours, (short)workrecord.getDurationHours()),
				Datatypes.member(ActivityAddWorkRecordParams.Member.durationMinutes, (short)workrecord.getDurationMinutes()),
				Datatypes.member(ActivityAddWorkRecordParams.Member.resource, resource),
				Datatypes.member(ActivityAddWorkRecordParams.Member.recordType, ActivitiesHelper.WORKRECORD_TYPE_USER_DEFINED),
				Datatypes.member(ActivityAddWorkRecordParams.Member.rateCurrency, (short)0),
				Datatypes.member(ActivityAddWorkRecordParams.Member.depotSelector, (short)0)
			);
			AddWorkAndExpenseRecordResult result = project.addWorkRecord(params);
			pm.currentTransaction().commit();
			WorkAndExpenseRecord _workRecord = result.getWorkRecord();
			pm.currentTransaction().begin();
			_workRecord.setUserString0(workrecord.getRateId());
			pm.currentTransaction().commit();
			return this.readWorkRecord(this.getWorkRecordId(_workRecord));
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
			throw new InternalServerErrorException();
		}
	}

	/* (non-Javadoc)
	 * @see org.opentdc.workrecords.ServiceProvider#readWorkRecord(java.lang.String)
	 */
	@Override
	public WorkRecordModel readWorkRecord(
		String id
	) throws NotFoundException {
		org.opencrx.kernel.activity1.jmi1.Segment activitySegment = this.getActivitySegment();
		WorkAndExpenseRecord _workRecord = null;
		try {
			_workRecord = this.getWorkRecord(activitySegment, id);
		} catch(Exception ignore) {
			new ServiceException(ignore).log();
		}
		if(_workRecord == null || Boolean.TRUE.equals(_workRecord.isDisabled())) {
			throw new NotFoundException("no work record with ID <" + id + "> found.");
		}
		WorkRecordModel workRecord = this.mapToWorkRecord(_workRecord);
		logger.info("readWorkRecord(" + id + "): " + workRecord);
		return workRecord;
	}

	/* (non-Javadoc)
	 * @see org.opentdc.workrecords.ServiceProvider#updateWorkRecord(java.lang.String, org.opentdc.workrecords.WorkRecordModel)
	 */
	@Override
	public WorkRecordModel updateWorkRecord(
		String id,
		WorkRecordModel workrecord
	) throws NotFoundException, ValidationException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.activity1.jmi1.Segment activitySegment = this.getActivitySegment();		
		WorkAndExpenseRecord _workRecord = null;
		try {
			_workRecord = this.getWorkRecord(activitySegment, id);
		} catch(Exception ignore) {}
		if(_workRecord == null || Boolean.TRUE.equals(_workRecord.isDisabled())) {
			throw new NotFoundException("no work record with ID <" + id + "> found.");
		}
		try {
			pm.currentTransaction().begin();
			_workRecord.setStartedAt(workrecord.getStartAt());
			_workRecord.setQuantity(
				new BigDecimal(workrecord.getDurationHours()).add(
					new BigDecimal(new Double(workrecord.getDurationMinutes()) / 60.0)
		        )
			);
			_workRecord.setName(workrecord.getComment());
			_workRecord.setBillable(workrecord.isBillable());			
			pm.currentTransaction().commit();
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
		}
		return this.readWorkRecord(id);
	}

	/* (non-Javadoc)
	 * @see org.opentdc.workrecords.ServiceProvider#deleteWorkRecord(java.lang.String)
	 */
	@Override
	public void deleteWorkRecord(
		String id
	) throws NotFoundException, InternalServerErrorException {
		PersistenceManager pm = this.getPersistenceManager();
		org.opencrx.kernel.activity1.jmi1.Segment activitySegment = this.getActivitySegment();		
		WorkAndExpenseRecord _workRecord = null;
		try {
			_workRecord = this.getWorkRecord(activitySegment, id);
		} catch(Exception ignore) {}
		if(_workRecord == null || Boolean.TRUE.equals(_workRecord.isDisabled())) {
			throw new NotFoundException("no work record with ID <" + id + "> found.");
		}
		try {
			pm.currentTransaction().begin();
			_workRecord.setDisabled(true);
			pm.currentTransaction().commit();
		} catch(Exception e) {
			new ServiceException(e).log();
			try {
				pm.currentTransaction().rollback();
			} catch(Exception ignore) {}
		}
	}
	
}
