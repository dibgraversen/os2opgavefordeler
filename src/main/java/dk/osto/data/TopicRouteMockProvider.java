package dk.osto.data;

import dk.osto.model.KLE;
import dk.osto.model.presentation.TopicRoutePO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class for providing mocked topic routes.
 * @author hlo@miracle.dk
 */
public class TopicRouteMockProvider {
	// employees
	private static final Integer NONE = 0;
	private static final Integer ALLAN = 1;
	private static final Integer JON = 2;

	// organisations
	private static final Integer UDVIKLING = 1;
	private static final Integer DIREKTIONSSEKRETARIATET = 2;
	private static final Integer IT = 3;
	private static final Integer DIGITALISERING = 4;

	// responsibles
	private static final Integer KULTUR_PLANLAEGNING_OG_ERHVERV_KENNETH_JENSEN = 1;
	private static final Integer DIGITALISERING_JON = 2;
	private static final Integer HR_OG_DIGITALISERING_EVA_DUE = 3;

	// KLE types
	private static final String MAIN = "main";
	private static final String GROUP = "group";
	private static final String TOPIC = "topic";

	public List<TopicRoutePO> employmentRoutes(int employment, String scope){
		List<TopicRoutePO> result = new ArrayList<TopicRoutePO>();
		result.add(createTopicRoutePO(1, 0, createKLE(1, "01", "Fysisk planlægning og naturbeskyttelse", MAIN, "Dette er en servicetekst til 01."),
				NONE, UDVIKLING, KULTUR_PLANLAEGNING_OG_ERHVERV_KENNETH_JENSEN));
		result.add(createTopicRoutePO(2, 1, createKLE(2, "01.06", "Geografiske informationssystemer", GROUP, "Dette er en servicetekst til 01.06."),
				ALLAN, IT, KULTUR_PLANLAEGNING_OG_ERHVERV_KENNETH_JENSEN));
		result.add(createTopicRoutePO(3, 2, createKLE(3, "01.06.00", "Geografiske informationssystemer i almindelighed", TOPIC, null),
				ALLAN, DIGITALISERING, DIGITALISERING_JON));
		result.add(createTopicRoutePO(4, 2, createKLE(4, "01.06.01", "Fikspunkter", TOPIC, null),
				ALLAN, DIGITALISERING, DIGITALISERING_JON));
		result.add(createTopicRoutePO(5, 0, createKLE(5, "85", "Kommunens administrative systemer", MAIN, "Dette er en servicetekst til 85."),
				JON, DIGITALISERING, HR_OG_DIGITALISERING_EVA_DUE));
		result.add(createTopicRoutePO(6, 5,createKLE(6, "85.04", "Blanketter og formularer", GROUP, null),
				JON, DIGITALISERING, DIGITALISERING_JON));
		result.add(createTopicRoutePO(7, 6, createKLE(7, "85.04.00", "Blanketter og formularer i almindelighed", TOPIC, null),
				JON, DIGITALISERING, DIGITALISERING_JON));
		result.add(createTopicRoutePO(8, 6, createKLE(8, "85.04.02", "KL autoriserede standardblanketter", TOPIC, null),
				NONE, DIREKTIONSSEKRETARIATET, DIGITALISERING_JON));
		return result;
	}

	private TopicRoutePO createTopicRoutePO(Integer id, Integer parent, KLE kle, int employee, int org, int responsible){
		TopicRoutePO result = new TopicRoutePO();
		result.setId(id);
		result.setParent(parent);
		result.setKle(kle);
		result.setEmployee(employee);
		result.setOrg(org);
		result.setResponsible(responsible);
		return result;
	}

	private KLE createKLE(int id, String number, String name, String type, String serviceText){
		KLE result = new KLE();
		result.setId(id);
		result.setNumber(number);
		result.setName(name);
		result.setType(type);
		result.setServiceText(serviceText);
		return result;
	}
}
