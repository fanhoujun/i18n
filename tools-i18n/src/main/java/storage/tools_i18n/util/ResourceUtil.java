package storage.tools_i18n.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONException;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.revwalk.RevCommit;

import storage.tools_i18n.model.Country;
import storage.tools_i18n.model.FolderModel;
import storage.tools_i18n.model.MetaData;

public class ResourceUtil {

	private static Logger log = Logger.getLogger(ResourceUtil.class.getName());

	/**
	 * download specific version files
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 * @throws TransportException
	 * @throws InvalidRemoteException
	 */
	public static void downloadPreviousCodes(String commitId) {
		log.log(Level.INFO,
				StringUtil.DELIMETER
						+ "Start donwloading previous applied translations version[commitId="
						+ commitId + "]");
		checkoutProject(Configuration.GIT_URL, commitId);
	}

	/**
	 * download latest necessary files, including metaData.json
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 * @throws TransportException
	 * @throws InvalidRemoteException
	 */
	public static MetaData downloadLatestCodes(String repoURL, String branchName) {
		String metadataFilePath = null;
		File metadataFile = null;
		log.log(Level.INFO, StringUtil.DELIMETER + "Donwloading...");
		MetaData metadata = new MetaData();
		metadata.setWorkspaceCommitId(checkoutProject(repoURL, branchName));
		log.log(Level.INFO,
				StringUtil.DELIMETER + "Donwloaded HEAD version[commitId="
						+ metadata.getWorkspaceCommitId() + "] from " + repoURL
						+ "[branch=" + branchName + "]");
		List<String> files = scanJsonFolders(repoURL,
				Configuration.METADATA_FILE);
		if (files.size() > 0) {
			metadataFilePath = files.get(0) + File.separator
					+ Configuration.METADATA_FILE;
			metadataFile = new File(metadataFilePath);
		}
		if (metadataFilePath != null && metadataFile.exists()) {
			Map<String, String> map = readJSON(metadataFilePath);
			metadata.setApplyId(map.get(MetaData.META_APPLY_ID));
			metadata.setExportId(map.get(MetaData.META_EXPORT_ID));
			metadata.setCreateDate(map.get(MetaData.META_CREATE_DATE));
			metadata.setCreatedBy(map.get(MetaData.META_CREATE_BY));
		}

		return metadata;
	}

	/**
	 * Read Json content from Json file and translate it to the Map
	 * 
	 * @param jsonFilePath
	 * @return
	 */
	public static Map<String, String> readJSON(String jsonFilePath) {
		File file = new File(jsonFilePath);
		Map<String, String> result = new LinkedHashMap<String, String>();
		if (!file.exists()) {
			return result;
		}
		String content = IoUtil.readText(file);
		try {
			JSONObject object = JSONObject.fromObject(content);
			parseJsonObjectToMap(object, "", result);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		StringBuffer sb = new StringBuffer(StringUtil.DELIMETER
				+ "Parsing file: ");
		sb.append(jsonFilePath);
		if (result.isEmpty()) {
			sb.append("\t" + jsonFilePath + " is empty");
		}
		log.log(Level.INFO, sb.toString());
		return result;
	}

	private static void parseJsonObjectToMap(JSONObject obj, String keyStr,
			Map<String, String> map) {
		Iterator iterator = obj.keys();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			Object value = obj.get(key);
			if (!StringUtil.isEmpty(keyStr)) {
				key = keyStr + "." + key;
			}
			if (value instanceof JSONObject) {
				parseJsonObjectToMap((JSONObject) value, key, map);
			} else if (!value.equals(JSONNull.getInstance())) {
				map.put(key, value.toString());
			}
		}
	}

	/**
	 * there are multiple josn_xx.json(json_en.json), read all of the keys in
	 * the files and return them as a Map
	 * 
	 * @param folders
	 * @param country
	 * @return Map all the keys of the same country code
	 */
	public static Map<String, String> readKeys(String folder, Country country) {
		Map<String, String> keys = new LinkedHashMap<String, String>();
		keys.putAll(readJSON(folder + File.separator + country.getCode()));
		return keys;
	}

	/**
	 * Scan the folders that contains locale_en.json file
	 * 
	 * @param rootFolder
	 * @return
	 */
	public static List<String> scanJsonFolders(String rootFolder,
			String fileName) {
		log.log(Level.INFO, StringUtil.DELIMETER + "Searching...");
		List<String> folderPaths = new ArrayList<String>();
		traverseFileInDirectory(rootFolder, fileName, folderPaths);

		String lastKey = "";
		for (int i = 0; i < folderPaths.size(); i++) {
			Pattern pattern = Pattern.compile("\\\\v\\d+\\\\");
			Matcher matcher = pattern.matcher(folderPaths.get(i));

			if (matcher.find()) {
				int index = matcher.start();
				String key = folderPaths.get(i).substring(0, index);
				if (lastKey.equals(key)) {
					folderPaths.remove(i - 1);
					i--;
				} else {
					lastKey = key;
				}
			}
		}
		StringBuffer sb = new StringBuffer("\nSearching file ");
		sb.append(fileName).append(" in folder ").append(rootFolder)
				.append("\n");
		for (String path : folderPaths) {
			sb.append("\t" + path + File.separator + fileName + "\n");
		}
		sb.append(StringUtil.DELIMETER + "End searching folders. "
				+ folderPaths.size() + " folders found.\n");
		log.log(Level.INFO, sb.toString());
		return folderPaths;
	}

	private static void traverseFileInDirectory(String filePath,
			String fileName, List<String> folderPaths) {
		File pFile = new File(filePath);
		File[] files = pFile.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				traverseFileInDirectory(file.toString(), fileName, folderPaths);
			}
			if (fileName.equals(file.getName())) {
				folderPaths.add(filePath);
			}
		}
	}

	/**
	 * Translate the Map to Json style content and put it to the Json file
	 * 
	 * @param pairs
	 * @param jsonFilePath
	 */
	public static void generateJsonFile(Map<String, String> pairs,
			String jsonFilePath) {
		log.log(Level.INFO, StringUtil.DELIMETER + "Generating " + jsonFilePath);
		JSONObject json = new JSONObject();
		for (Entry<String, String> entry : pairs.entrySet()) {
			String key = entry.getKey();
			String[] splittedKey = key.split("\\.");
			JSONObject nestedObject = json;

			for (int i = 0; i < splittedKey.length - 1; i++) {
				if (!nestedObject.has(splittedKey[i])) {
					nestedObject.accumulate(splittedKey[i], new JSONObject());
				}
				nestedObject = nestedObject.getJSONObject(splittedKey[i]);
			}
			nestedObject.accumulate(splittedKey[splittedKey.length - 1],
					entry.getValue());
		}
		IoUtil.write(json.toString(4), new File(jsonFilePath));
	}

	/**
	 * Clone a Git repo to a specified path and checkout to a specified branch
	 * or commit ID
	 * 
	 * @param repoURL
	 * @param localPath
	 * @param commitId
	 * @return
	 * @throws InvalidRemoteException
	 * @throws TransportException
	 * @throws GitAPIException
	 * @throws IOException
	 */
	private static String checkoutProject(String repoURL, String commitId) {
		if (StringUtil.isEmpty(commitId)) {
			throw new RuntimeException("commitId should not empty!");
		}
		File repo = new File(repoURL);
		if (!repo.exists() || repo.listFiles().length <= 0) {
			log.log(Level.SEVERE, "Can not find the Git repo: " + repoURL);
			throw new RuntimeException("Can not find the Git repo: " + repoURL);
		}

		Git git = null;
		try {
			git = Git.open(repo);
		} catch (IOException e) {
			log.log(Level.SEVERE, "Open repo failed! Error: " + e);
			throw new RuntimeException("Open repo failed! Error: " + e);
		}

		// Checkout, commitId can be branch name or commit ID
		if (!StringUtil.isEmpty(commitId)) {
			CheckoutCommand cc = git.checkout();
			cc.setName(commitId);
			try {
				cc.call();
			} catch (Exception e) {
				log.log(Level.SEVERE, "Checkout failed! Error: " + e);
				throw new RuntimeException("Checkout failed! Error: " + e);
			}
		}

		// If the commitId is empty(means the current branch is master) or a
		// branch name, get the latest commit ID
		try {
			Iterable<RevCommit> logs = git.log().call();
			RevCommit rc = logs.iterator().next();
			commitId = rc.getName();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Get commit ID failed! Error: " + e);
			throw new RuntimeException("Get commit ID failed! Error: " + e);
		}
		return commitId;
	}

	public static List<FolderModel> loadFolders() {
		// scan current version folder structure
		List<String> jsonFolders = ResourceUtil.scanJsonFolders(
				Configuration.GIT_URL, Country.ENGLISH.getCode());

		List<FolderModel> folders = new ArrayList();
		Set<String> currentModules = new HashSet<String>();
		// read current version English data
		for (String jsonFolder : jsonFolders) {
			currentModules.add(jsonFolder);
			Map<String, String> englishPair = ResourceUtil.readKeys(jsonFolder,
					Country.ENGLISH);
			FolderModel folderModel = new FolderModel();
			folderModel.setEnglishPair(englishPair);
			// avoid NullPointerException
			folderModel.setOldEnPair(new LinkedHashMap<String, String>());
			folderModel.setFolder(jsonFolder);
			folders.add(folderModel);
		}
		if (currentModules.size() != jsonFolders.size()) {
			log.warning("Module name duplicated after simplified, Please modify the configuration \"IGNORE_KEY_WRODS\" in the properties file and run this tool again."
					+ "If it not works, please delete the keywords and run this tool again, thanks.");
		}
		// if applied translation before, download the last applied version

		for (FolderModel model : folders) {
			Map<String, Map<String, String>> allLocals = new LinkedHashMap<String, Map<String, String>>();
			for (Country country : Country.locals()) {
				Map<String, String> values = ResourceUtil.readKeys(
						model.getFolder(), country);
				allLocals.put(country.getCode(), values);
			}
			model.setAllLocals(allLocals);
		}
		return folders;
	}

	public static void readOldEnPairs(String commitId, List<FolderModel> modles) {
		if (StringUtil.isEmpty(commitId)) {
			return;
		}
		ResourceUtil.downloadPreviousCodes(commitId);
		// scan previous version folder structure
		for (FolderModel model : modles) {
			Map<String, String> oldEnPair = ResourceUtil.readKeys(
					model.getFolder(), Country.ENGLISH);
			model.setOldEnPair(oldEnPair);
		}
		ResourceUtil.downloadLatestCodes(Configuration.GIT_URL,
				Configuration.DEFAULT_BRANCH);
	}

}
