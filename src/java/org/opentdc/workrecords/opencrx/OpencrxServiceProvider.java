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

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.openmdx.base.exception.ServiceException;
import org.opentdc.opencrx.AbstractOpencrxServiceProvider;
import org.opentdc.service.exception.DuplicateException;
import org.opentdc.service.exception.NotFoundException;
import org.opentdc.service.exception.NotImplementedException;
import org.opentdc.workrecords.ServiceProvider;
import org.opentdc.workrecords.WorkRecordModel;

public class OpencrxServiceProvider extends AbstractOpencrxServiceProvider implements ServiceProvider {
	
	public static final short ACTIVITY_GROUP_TYPE_PROJECT = 40;
	public static final short ACCOUNT_ROLE_CUSTOMER = 100;
	public static final short ACTIVITY_CLASS_INCIDENT = 2;
	public static final short ICAL_TYPE_NA = 0;
	public static final short ICAL_CLASS_NA = 0;
	public static final short ICAL_TYPE_VEVENT = 1;

	// instance variables
	protected static final Logger logger = Logger.getLogger(OpencrxServiceProvider.class.getName());
	
	public OpencrxServiceProvider(
		ServletContext context,
		String prefix
	) throws ServiceException, NamingException {
		super(context, prefix);
	}

	/******************************** workrecord *****************************************/
	/**
	 * List all workrecords.
	 * 
	 * @return a list of all workrecords.
	 */
	@Override
	public ArrayList<WorkRecordModel> listWorkRecords(
		String queryType,
		String query,
		long position,
		long size
	) {
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
	public WorkRecordModel updateWorkRecord(
		String id,
		WorkRecordModel workrecord
	) throws NotFoundException {
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

}
