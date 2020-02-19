package org.egov.waterConnection.service;



import java.util.LinkedList;
import java.util.List;
import org.egov.tracer.model.CustomException;
import org.egov.waterConnection.constants.WCConstants;
import org.egov.waterConnection.model.Difference;
import org.egov.waterConnection.model.WaterConnection;
import org.egov.waterConnection.model.WaterConnectionRequest;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ValueChange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class DiffService {
	
	@Autowired
	private EditNotificationService editNotificationService;
	/**
	 * Creates a list of Difference object between the update and search
	 * 
	 * @param request The water connection request for update
	 * @param searchResult The searched result 
	 * @return List of Difference object
	 */
	public void checkDifferenceAndSendEditNotification(WaterConnectionRequest request, WaterConnection searchResult) {
		try {
			WaterConnection updateConnection = request.getWaterConnection();
			Difference diff = new Difference();
			diff.setId(updateConnection.getId());
			diff.setFieldsChanged(getUpdateFields(updateConnection, searchResult));
			diff.setClassesAdded(getObjectsAdded(updateConnection, searchResult));
			diff.setClassesRemoved(getObjectsRemoved(updateConnection, searchResult));
			if (!CollectionUtils.isEmpty(diff.getFieldsChanged()) || !CollectionUtils.isEmpty(diff.getClassesAdded())
					|| !CollectionUtils.isEmpty(diff.getClassesRemoved())) {
				editNotificationService.sendEditNotification(request);
			}
		} catch (Exception ex) {
			StringBuilder builder = new StringBuilder("Edit Notification Error!!");
			log.error(builder.toString(), ex);
		}
	}
	
	/**
	 * Check updated fields
	 * 
	 * @param updateConnection
	 * @param searchResult
	 * @return List of updated fields
	 */
	private List<String> getUpdateFields(WaterConnection updateConnection, WaterConnection searchResult) {
		Javers javers = JaversBuilder.javers().build();
		Diff diff = javers.compare(updateConnection, searchResult);
		List<String> updatedValues = new LinkedList<>();
		List<ValueChange> changes = diff.getChangesByType(ValueChange.class);
		if (CollectionUtils.isEmpty(changes))
			return updatedValues;
		changes.forEach(change -> {
			if (!WCConstants.FIELDS_TO_IGNORE.contains(change.getPropertyName())) {
				updatedValues.add(change.getPropertyName());
            }
		});
		log.debug("Updated Fields :----->  "+ updatedValues.toString());
		return updatedValues;
	}
	/**
	 * Check for added new object
	 * 
	 * @param updateConnection
	 * @param searchResult
	 * @return list of added object
	 */
	@SuppressWarnings("unchecked")
	private List<String> getObjectsAdded(WaterConnection updateConnection, WaterConnection searchResult) {
		Javers javers = JaversBuilder.javers().build();
		Diff diff = javers.compare(updateConnection, searchResult);
		List<NewObject> objectsAdded = diff.getObjectsByChangeType(NewObject.class);
		List<String> classModified = new LinkedList<>();
		if (CollectionUtils.isEmpty(objectsAdded))
			return classModified;
		for(Object object: objectsAdded) {
			String className = object.getClass().toString()
					.substring(object.getClass().toString().lastIndexOf('.') + 1);
			if (!classModified.contains(className))
					classModified.add(className);
		}
		log.debug("Class Modified :----->  "+ classModified.toString());
		return classModified;
	}
	
	/**
	 * 
	 * @param updateConnection
	 * @param searchResult
	 * @return List of added or removed object
	 */
    private List<String> getObjectsRemoved(WaterConnection updateConnection, WaterConnection searchResult) {

        Javers javers = JaversBuilder.javers().build();
        Diff diff = javers.compare(updateConnection, searchResult);
        List<ValueChange> changes = diff.getChangesByType(ValueChange.class);
        List<String> classRemoved = new LinkedList<>();
        if (CollectionUtils.isEmpty(changes))
            return classRemoved;
//        changes.forEach(change -> {
//            if (change.getPropertyName().equalsIgnoreCase(VARIABLE_ACTIVE)
//                    || change.getPropertyName().equalsIgnoreCase(VARIABLE_USERACTIVE)) {
//                classRemoved.add(getObjectClassName(change.getAffectedObject().toString()));
//            }
//        });
        return classRemoved;
    }
    
    /**
     * Extracts the class name from the affectedObject string representation
     * @param affectedObject The object which is removed
     * @return Name of the class of object removed
     */
	private String getObjectClassName(String affectedObject) {
		String className = null;
		try {
			String firstSplit = affectedObject.substring(affectedObject.lastIndexOf('.') + 1);
			className = firstSplit.split("@")[0];
		} catch (Exception e) {
			throw new CustomException("OBJECT CLASS NAME PARSE ERROR", "Failed to fetch notification");
		}
		return className;
	}
}
