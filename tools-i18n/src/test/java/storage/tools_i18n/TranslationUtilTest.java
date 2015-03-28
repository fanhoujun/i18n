package storage.tools_i18n;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import storage.tools_i18n.model.MetaData;
import storage.tools_i18n.util.Configuration;
import storage.tools_i18n.util.IoUtil;
import storage.tools_i18n.util.TranslationUtil;

public class TranslationUtilTest {

	public static final String UNITTEST_FILE_PATH = Thread.currentThread()
			.getContextClassLoader().getResource(".").getPath()
			+ "unit-test";

	@BeforeClass
	public static void createJsonFiles() throws IOException {
		String path1 = UNITTEST_FILE_PATH + File.separator + "v2"
				+ File.separator + "i18n";
		String path2 = UNITTEST_FILE_PATH + File.separator + "v3"
				+ File.separator + "i18n";
		String jsonContent = "{\"NEO\":{\"BOOTSTRAP\":{\"LOADER_ERROR\":\"Loader Error:\",\"NOTIFICATION\":{\"LATEST_TEN_MESSAGE\":\"Your latest notifications:\",\"MARK_ALL_READ\":\"Mark as Read\"}}}}";

		File folder1 = new File(path1);
		File folder2 = new File(path2);
		File file1, file2 = null;
		if (!folder1.exists()) {
			folder1.mkdirs();
			file1 = new File(folder1, "locale_en.json");
			file1.createNewFile();
		}
		if (!folder2.exists()) {
			folder2.mkdirs();
			file2 = new File(folder2, "locale_en.json");
			file2.createNewFile();
		}

		IoUtil.write(jsonContent, file2);
	}

	@Test
	public void downloadPreviousCodes() {
		String commitId = "76fb7c4db49b3eb98dfab5d0291a7f32dd371c59";
		TranslationUtil.downloadPreviousCodes(commitId);
		File file = new File(Configuration.GIT_URL);
		assertTrue(file.exists());
		assertTrue(file.listFiles().length > 0);
	}

	@Test
	public void downloadLatestCodes() {
		MetaData metadata = new MetaData();

		String commitId = "origin/development";
		metadata = TranslationUtil.downloadLatestCodes(Configuration.GIT_URL,
				commitId);
		File file = new File(Configuration.GIT_URL);
		assertTrue(file.exists());
		assertTrue(file.listFiles().length > 0);
		assertNotNull(metadata.getWorkspaceCommitId());
	}

	// @Test
	// public void scanJsonFolders() {
	// List<String> list = TranslationUtil.scanJsonFolders(UNITTEST_FILE_PATH,
	// Constant.LOCALE_EN);
	// assertTrue(list.size() == 1);
	// }
	//
	// @Test
	// public void readJSON() {
	// List<String> list = TranslationUtil.scanJsonFolders(UNITTEST_FILE_PATH,
	// Constant.LOCALE_EN);
	// Map<String, String> map = TranslationUtil.readJSON(list.get(0) +
	// File.separator + "locale_en.json");
	//
	// assertTrue(map.size() == 3);
	// assertEquals(map.get("NEO.BOOTSTRAP.LOADER_ERROR"), "Loader Error:");
	// }

	@Test
	public void generateJsonFile() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("NEO.BOOTSTRAP.LOADER_ERROR", "Loader Error:");
		map.put("NEO.BOOTSTRAP.NOTIFICATION.LATEST_TEN_MESSAGE",
				"Your latest notifications:");
		map.put("NEO.BOOTSTRAP.NOTIFICATION.MARK_ALL_READ", "Mark as Read");

		String jsonFilePath = UNITTEST_FILE_PATH + File.separator
				+ "unit-test.json";

		TranslationUtil.generateJsonFile(map, jsonFilePath);

		map = TranslationUtil.readJSON(jsonFilePath);

		assertTrue(map.size() == 3);
		assertEquals(map.get("NEO.BOOTSTRAP.LOADER_ERROR"), "Loader Error:");
	}

	@AfterClass
	public static void removeRepo() {
		File unitTestFolder = new File(UNITTEST_FILE_PATH);

		if (unitTestFolder.exists()) {
			deleteAll(unitTestFolder);
		}
	}

	public static void deleteAll(File file) {
		if (file.isFile() || file.list().length == 0) {
			file.delete();
		} else {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteAll(files[i]);
				files[i].delete();
			}

			if (file.exists())
				file.delete();
		}
	}
}
