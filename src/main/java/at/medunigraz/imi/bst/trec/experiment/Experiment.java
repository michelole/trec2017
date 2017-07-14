package at.medunigraz.imi.bst.trec.experiment;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import at.medunigraz.imi.bst.trec.evaluator.TrecEval;
import at.medunigraz.imi.bst.trec.evaluator.TrecWriter;
import at.medunigraz.imi.bst.trec.model.Result;
import at.medunigraz.imi.bst.trec.model.ResultList;
import at.medunigraz.imi.bst.trec.model.Topic;
import at.medunigraz.imi.bst.trec.model.TopicSet;
import at.medunigraz.imi.bst.trec.query.Query;
import at.medunigraz.imi.bst.trec.stats.CSVStatsWriter;
import at.medunigraz.imi.bst.trec.stats.XMLStatsWriter;

public class Experiment extends Thread {

	private static final Logger LOG = LogManager.getLogger();

	@Deprecated
	private String id = null;
	
	private Query decorator;
	
	private Task task;
	
	private GoldStandard goldStandard;
	
	private TrecEval trecEval;
	
	public static enum Task {
		CLINICAL_TRIALS, PUBMED
	}
	
	public static enum GoldStandard {
		EXAMPLE, EXTRA, FINAL
	}

	@Override
	public void run() {
		final String collection = getExperimentId().substring(0, getExperimentId().indexOf('-'));

		final String name = getExperimentId() + " with decorator " + decorator.getName();

		LOG.info("Running collection " + name + "...");

		File example = new File(CSVStatsWriter.class.getResource("/topics/" + collection + ".xml").getPath());
		TopicSet topicSet = new TopicSet(example);

		File output = new File("results/" + getExperimentId() + ".trec_results");
		TrecWriter tw = new TrecWriter(output);

		// TODO DRY Issue #53
		Set<ResultList> resultListSet = new HashSet<>();
		for (Topic topic : topicSet.getTopics()) {
			List<Result> results = decorator.query(topic);

			ResultList resultList = new ResultList(topic);
			resultList.setResults(results);
			resultListSet.add(resultList);
		}

		tw.write(resultListSet);
		tw.close();

		File goldStandard = new File(CSVStatsWriter.class.getResource("/gold-standard/" + getExperimentId() + ".qrels").getPath());
		trecEval = new TrecEval(goldStandard, output);

		LOG.info("Got NDCG: " + trecEval.getNDCG() + " for collection " + name);
		LOG.trace(trecEval.getMetricsByTopic("all"));
	}
	
	public void writeFullStatsToCSV() {
		CSVStatsWriter csw = new CSVStatsWriter(new File("stats/" + getExperimentId() + ".csv"));
		csw.write(trecEval.getMetrics());
		csw.close();
	}
	
	public void writeFullStatsToXML() {
		XMLStatsWriter xsw = new XMLStatsWriter(new File("stats/" + getExperimentId() + ".xml"));
		xsw.write(trecEval.getMetrics());
		xsw.close();
	}
	
	@Deprecated
	public void setExperimentId(String id) {
		this.id = id;
	}

	public void setDecorator(Query decorator) {
		this.decorator = decorator;
	}

	public String getExperimentId() {
		if (id != null) {
			return id;
		}
		
		return getCollectionName() + "-" + getShortTaskName();
		
	}
	
	public void setTask(Task task) {
		this.task = task;
	}

	public void setGoldStandard(GoldStandard goldStandard) {
		this.goldStandard = goldStandard;
	}

	public String getCollectionName() {
		switch (goldStandard) {
		case EXAMPLE:
			return "example";
		case EXTRA:
			return "extra";
		case FINAL:
			return "topics2017";
		default:
			return "";
		}
	}
	
	public String getTaskName() {
		switch (task) {
		case CLINICAL_TRIALS:
			return "clinicaltrials";
		case PUBMED:
			return "trec";
		default:
			return "";
		}
	}
	
	public String getShortTaskName() {
		switch (task) {
		case CLINICAL_TRIALS:
			return "ct";
		case PUBMED:
			return "pmid";
		default:
			return "";
		}
	}

	public Query getDecorator() {
		return decorator;
	}
	
	
}
