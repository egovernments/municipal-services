package org.egov.waterConnection.service;



import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.egov.waterConnection.constants.WCConstants;
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
			if (!CollectionUtils.isEmpty(getUpdateFields(updateConnection, searchResult))
					|| !CollectionUtils.isEmpty(getObjectsAdded(updateConnection, searchResult))
					|| !CollectionUtils.isEmpty(getObjectsRemoved(updateConnection, searchResult))) {
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
		List<ValueChange> changes = diff.getChangesByType(ValueChange.class);
		if (CollectionUtils.isEmpty(changes))
			return Collections.emptyList();
		List<String> updatedValues = new LinkedList<>();
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
		if (CollectionUtils.isEmpty(objectsAdded))
			return Collections.emptyList();
		List<String> classModified = new LinkedList<>();
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
        if (CollectionUtils.isEmpty(changes))
            return Collections.emptyList();
        List<String> classRemoved = new LinkedList<>();
//        changes.forEach(change -> {
//            if (change.getPropertyName().equalsIgnoreCase(VARIABLE_ACTIVE)
//                    || change.getPropertyName().equalsIgnoreCase(VARIABLE_USERACTIVE)) {
//                classRemoved.add(getObjectClassName(change.getAffectedObject().toString()));
//            }
//        });
        return classRemoved;
    }
    
}
