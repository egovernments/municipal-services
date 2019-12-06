package org.egov.bpa.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.bpa.web.models.BPA;
import org.egov.bpa.web.models.BPARequest;
import org.egov.bpa.web.models.Difference;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Diff;
import org.javers.core.diff.changetype.ValueChange;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class DiffService {

	/**
	 * Creates a list of Difference object between the update and search
	 *
	 * @param request
	 *            The bpaRequest for update
	 * @param searchResult
	 *            The searched bpa corresponding to the request
	 * @return List of Difference object
	 */
	public Map<String, Difference> getDifference(BPARequest request,
			List<BPA> searchResult) {

		BPA bpa = request.getBPA();
		Map<String, Difference> diffMap = new LinkedHashMap<>();
		Map<String, BPA> idToBpaMap = new HashMap<>();

		idToBpaMap.put(bpa.getId(), bpa);

		BPA bpaFromSearch;

		bpaFromSearch = idToBpaMap.get(bpa.getId());
		Difference diff = new Difference();
		diff.setId(bpa.getId());
		diff.setFieldsChanged(getUpdatedFields(bpa, bpaFromSearch));
		diff.setClassesRemoved(getObjectsRemoved(bpa, bpaFromSearch));
		diffMap.put(bpa.getId(), diff);

		return diffMap;
	}

	/**
	 * Gives the field names whose values are different in the two classes
	 *
	 * @param bpaFromUpdate
	 *            bpa from update request
	 * @param bpaFromSearch
	 *            bpa from db on which update is called
	 * @return List of variable names which are changed
	 */
	private List<String> getUpdatedFields(BPA bpa,
			BPA bpaFromSearch) {

		Javers javers = JaversBuilder.javers().build();

		Diff diff = javers.compare(bpa, bpaFromSearch);
		List<ValueChange> changes = diff.getChangesByType(ValueChange.class);

		List<String> updatedFields = new LinkedList<>();

		if (CollectionUtils.isEmpty(changes))
			return updatedFields;

		changes.forEach(change -> {
			/*
			 * if (!FIELDS_TO_IGNORE.contains(change.getPropertyName())) {
			 * updatedFields.add(change.getPropertyName()); }
			 */
		});
		return updatedFields;

	}

	
	/**
	 * Gives the names of the classes whose object are added or removed between
	 * the given bpa
	 *
	 * @param bpaFromUpdate
	 *            bpa from update request
	 * @param bpaFromSearch
	 *            bpa  from db on which update is called
	 * @return Names of Classes added or removed during update
	 */
	private List<String> getObjectsRemoved(BPA bpa,
			BPA bpaFromSearch) {

		Javers javers = JaversBuilder.javers().build();
		Diff diff = javers.compare(bpa, bpaFromSearch);
		List<ValueChange> changes = diff.getChangesByType(ValueChange.class);

		List<String> classRemoved = new LinkedList<>();

		if (CollectionUtils.isEmpty(changes))
			return classRemoved;

		
		return classRemoved;
	}


}
