
package org.openmrs.module.htmlformentry.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.OpenmrsObject;
import org.openmrs.OrderFrequency;
import org.openmrs.OrderType;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.HtmlForm;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.schema.CareSettingAnswer;
import org.openmrs.module.htmlformentry.schema.DrugOrderAnswer;
import org.openmrs.module.htmlformentry.schema.DrugOrderField;
import org.openmrs.module.htmlformentry.schema.HtmlFormField;
import org.openmrs.module.htmlformentry.schema.HtmlFormSchema;
import org.openmrs.module.htmlformentry.schema.ObsFieldAnswer;
import org.openmrs.module.htmlformentry.schema.OrderFrequencyAnswer;
import org.openmrs.module.htmlformentry.schema.OrderTypeAnswer;
import org.openmrs.module.htmlformentry.substitution.Substituter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This is a subclass of AttributeDescriptor that allows for resolving openmrs object dependencies
 * and substituting them out for Metadata Sharing
 */
public class DrugOrderTagAttributeDescriptor extends AttributeDescriptor {
	
	public DrugOrderTagAttributeDescriptor() {
		super("value", OpenmrsObject.class);
	}
	
	public HtmlForm performSubstitutions(HtmlForm htmlForm, Substituter substituter, Map<OpenmrsObject, OpenmrsObject> m) {
		try {
			String xml = htmlForm.getXmlData();
			Document doc = HtmlFormEntryUtil.stringToDocument(xml);
			Node content = HtmlFormEntryUtil.findChild(doc, "htmlform");
			handleNode(content, null, substituter, m);
			String updatedXml = HtmlFormEntryUtil.documentToString(doc);
			htmlForm.setXmlData(updatedXml);
			return htmlForm;
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to perform drug order tag substitutions", e);
		}
	}
	
	private void handleNode(Node node, Class<? extends OpenmrsObject> currentPropertyType, Substituter substituter,
	        Map<OpenmrsObject, OpenmrsObject> substitutionMap) {
		if (node != null) {
			String name = node.getNodeName();
			if (name.equalsIgnoreCase("drugOrder")) {
				handleDrugOrderNode(node, substituter, substitutionMap);
			} else if (name.equalsIgnoreCase("orderProperty")) {
				String property = HtmlFormEntryUtil.getNodeAttribute(node, "name", "");
				currentPropertyType = DrugOrderTagHandler.PROPERTIES.get(property);
				if (currentPropertyType != null) {
					updateValueNode(node, currentPropertyType, substituter, substitutionMap);
				}
			} else if (name.equalsIgnoreCase("option")) {
				if (currentPropertyType != null) {
					updateValueNode(node, currentPropertyType, substituter, substitutionMap);
				}
			}
			if (node.getChildNodes() != null) {
				NodeList children = node.getChildNodes();
				for (int i = 0; i < children.getLength(); ++i) {
					handleNode(children.item(i), currentPropertyType, substituter, substitutionMap);
				}
			}
		}
	}
	
	private void handleDrugOrderNode(Node node, Substituter substituter, Map<OpenmrsObject, OpenmrsObject> substitutionMap) {
		// TODO: Handle drugOrder tag, with legacy options for backwards compatibility
	}
	
	private void updateValueNode(Node node, Class<? extends OpenmrsObject> type, Substituter substituter,
	        Map<OpenmrsObject, OpenmrsObject> substitutionMap) {
		if (node != null) {
			Node valueNode = node.getAttributes().getNamedItem("value");
			if (valueNode != null) {
				String propertyValue = valueNode.getTextContent();
				if (StringUtils.isNotBlank(propertyValue)) {
					String replacementVal = substituter.substitute(propertyValue, type, substitutionMap);
					valueNode.setTextContent(replacementVal);
				}
			}
		}
	}
	
	public Map<Class<? extends OpenmrsObject>, Set<OpenmrsObject>> getDependencies(HtmlForm htmlForm) {
		Map<Class<? extends OpenmrsObject>, Set<OpenmrsObject>> ret = new HashMap<>();
		try {
			HtmlFormSchema schema = HtmlFormEntryUtil.getHtmlFormSchema(htmlForm, FormEntryContext.Mode.ENTER);
			for (HtmlFormField field : schema.getAllFields()) {
				if (field instanceof DrugOrderField) {
					DrugOrderField f = (DrugOrderField) field;
					if (f.getDrugOrderAnswers() != null) {
						for (DrugOrderAnswer a : f.getDrugOrderAnswers()) {
							addDependency(ret, Drug.class, a.getDrug());
						}
					}
					if (f.getCareSettingAnswers() != null) {
						for (CareSettingAnswer a : f.getCareSettingAnswers()) {
							addDependency(ret, CareSetting.class, a.getCareSetting());
						}
					}
					if (f.getOrderTypeAnswers() != null) {
						for (OrderTypeAnswer a : f.getOrderTypeAnswers()) {
							addDependency(ret, OrderType.class, a.getOrderType());
						}
					}
					if (f.getDoseUnitAnswers() != null) {
						for (ObsFieldAnswer a : f.getDoseUnitAnswers()) {
							addDependency(ret, Concept.class, a.getConcept());
						}
					}
					if (f.getRouteAnswers() != null) {
						for (ObsFieldAnswer a : f.getRouteAnswers()) {
							addDependency(ret, Concept.class, a.getConcept());
						}
					}
					if (f.getFrequencyAnswers() != null) {
						for (OrderFrequencyAnswer a : f.getFrequencyAnswers()) {
							addDependency(ret, OrderFrequency.class, a.getOrderFrequency());
						}
					}
					if (f.getDurationUnitAnswers() != null) {
						for (ObsFieldAnswer a : f.getDurationUnitAnswers()) {
							addDependency(ret, Concept.class, a.getConcept());
						}
					}
					if (f.getQuantityUnitAnswers() != null) {
						for (ObsFieldAnswer a : f.getQuantityUnitAnswers()) {
							addDependency(ret, Concept.class, a.getConcept());
						}
					}
					if (f.getDiscontinuedReasonQuestion() != null) {
						addDependency(ret, Concept.class, f.getDiscontinuedReasonQuestion());
					}
					if (f.getDiscontinuedReasonAnswers() != null) {
						for (ObsFieldAnswer a : f.getDiscontinuedReasonAnswers()) {
							addDependency(ret, Concept.class, a.getConcept());
						}
					}
				}
			}
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to get dependencies for form", e);
		}
		return ret;
	}
	
	private void addDependency(Map<Class<? extends OpenmrsObject>, Set<OpenmrsObject>> m,
	        Class<? extends OpenmrsObject> type, OpenmrsObject obj) {
		Set<OpenmrsObject> s = m.computeIfAbsent(type, k -> new HashSet<>());
		s.add(obj);
	}
}
