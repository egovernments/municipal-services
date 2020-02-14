package org.egov.swService.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.swService.model.Difference;
import org.egov.swService.model.SewerageConnection;
import org.egov.swService.model.SewerageConnectionRequest;
import org.egov.swService.util.SWConstants;
import org.egov.tracer.model.CustomException;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ValueChange;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class DiffService {

	/**
	 * Creates a list of Difference object between the update and search
	 * 
	 * @param request The sewerage connection request for update
	 * @param searchResult The searched result 
	 * @return List of Difference object
	 */
	public Map<String, Difference> getDifference(SewerageConnectionRequest request, SewerageConnection searchResult) {
		SewerageConnection updateConnection = request.getSewerageConnection();
		Map<String, Difference> diffMap = new LinkedHashMap<>();
		Difference diff = new Difference();
		diff.setId(updateConnection.getId());
		diff.setFieldsChanged(getUpdateFields(updateConnection, searchResult));
		diff.setClassesAdded(getObjectsAdded(updateConnection, searchResult));
		diff.setClassesRemoved(getObjectsRemoved(updateConnection, searchResult));
		diffMap.put(updateConnection.getId(), diff);
		return diffMap;
	}
	
	/**
	 * Check updated fields
	 * 
	 * @param updateConnection
	 * @param searchResult
	 * @return List of updated fields
	 */
	private List<String> getUpdateFields(SewerageConnection updateConnection, SewerageConnection searchResult) {
		Javers javers = JaversBuilder.javers().build();
		Diff diff = javers.compare(updateConnection, searchResult);
		List<String> updatedValues = new LinkedList<>();
		List<ValueChange> changes = diff.getChangesByType(ValueChange.class);
		if (CollectionUtils.isEmpty(changes))
			return updatedValues;
		changes.forEach(change -> {
			if (!SWConstants.FIELDS_TO_IGNORE.contains(change.getPropertyName())) {
				updatedValues.add(change.getPropertyName());
            }
		});
		log.info("Updated Fields :----->  "+ updatedValues.toString());
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
	private List<String> getObjectsAdded(SewerageConnection updateConnection, SewerageConnection searchResult) {
		Javers javers = JaversBuilder.javers().build();
		Diff diff = javers.compare(updateConnection, searchResult);
		List<NewObject> objectsAdded = diff.getObjectsByChangeType(NewObject.class);
		List<String> classModified = new LinkedList<>();
		if (CollectionUtils.isEmpty(objectsAdded))
			return classModified;
		objectsAdded.forEach(object -> {
			String className = object.getClass().toString()
					.substring(object.getClass().toString().lastIndexOf('.') + 1);
			if (!classModified.contains(className))
					classModified.add(className);
		});
		return classModified;
	}
	
	/**
	 * 
	 * @param updateConnection
	 * @param searchResult
	 * @return List of added or removed object
	 */
    private List<String> getObjectsRemoved(SewerageConnection updateConnection, SewerageConnection searchResult) {

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