package storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import storage.utils.Serializer;


public final class StorageItf {
	
	// Logger According to Loggers.STORAGE
    private static final Logger logger = LogManager.getLogger("integratedtoolkit.Storage");

    private static final String ERROR_HOSTNAME						= "ERROR: Cannot find localhost hostname";
	private static final String ERROR_CREATE_WD 					= "ERROR: Cannot create WD ";
	private static final String ERROR_ERASE_WD 						= "ERROR: Cannot erase WD";
	private static final String ERROR_CONFIGURATION_NOT_FOUND 		= "ERROR: Configuration file not found";
	private static final String ERROR_CONFIGURATION_CANNOT_OPEN 	= "ERROR: Cannot open configuration file";
	private static final String ERROR_NO_PSCO						= "ERROR: Cannot found PSCO in master with id=";
	private static final String ERROR_NEW_REPLICA 					= "ERROR: Cannot create new replica of PSCO with id=";
	private static final String ERROR_NEW_VERSION 					= "ERROR: Cannot create new version of PSCO with id=";
	private static final String ERROR_DESERIALIZE 					= "ERROR: Cannot deserialize object with id=";
	private static final String ERROR_SERIALIZE						= "ERROR: Cannot serialize object to id=";
	

	private static final String BASE_WORKING_DIR = File.separator + "tmp" + File.separator + "PSCO" + File.separator;
	
	private static final String MASTER_HOSTNAME;
	private static final String MASTER_WORKING_DIR;
	
	private static final LinkedList<String> hostnames = new LinkedList<String>();
	
	static {
		String hostname = null;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			System.err.println(ERROR_HOSTNAME);
			e.printStackTrace();
			System.exit(1);
		}
		
		MASTER_HOSTNAME = hostname;
		MASTER_WORKING_DIR = BASE_WORKING_DIR + File.separator + MASTER_HOSTNAME + File.separator;
	}

	/**
	 * Constructor
	 */
	public StorageItf() {
	}

	/**
	 * Initializes the persistent storage
	 * Configuration file must contain all the worker hostnames, one by line
	 * 
	 * @param storageConf
	 * @throws StorageException
	 */
	public static void init(String storageConf) throws StorageException {
		logger.info("[LOG] Storage Initialization");
		
		// Add master hostname
		hostnames.add(MASTER_HOSTNAME);
		
		// Add worker' hostnames (by storageConf)
		logger.info("[LOG] Configuration received: " + storageConf);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(storageConf));
			String line;
			while ((line = br.readLine()) != null) {
			   hostnames.add(line);
			}
		} catch (FileNotFoundException e) {
			throw new StorageException(ERROR_CONFIGURATION_NOT_FOUND, e);
		} catch (IOException e) {
			throw new StorageException(ERROR_CONFIGURATION_CANNOT_OPEN, e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				// No need to handle such exceptions
			}
		}
		
		// Create base WD if needed
		File wd = new File(BASE_WORKING_DIR);
		if (!wd.exists()) {
			try {
				wd.mkdir();
			} catch (SecurityException se) {
				throw new StorageException(ERROR_CREATE_WD + BASE_WORKING_DIR, se);
			}
		}
		
		// Create specific WD
		for (String hostname : hostnames) {
			logger.debug("[LOG] Hostname: " + hostname);
			String hostPath = BASE_WORKING_DIR + hostname;
			File hostWD = new File(hostPath);
			if (!hostWD.exists()) {
				try {
					hostWD.mkdir();
				} catch (SecurityException se) {
					throw new StorageException(ERROR_CREATE_WD + hostPath, se);
				}
			}
		}

		// Log Initialization
		logger.info("[LOG] Storage Initialization finished");
	}

	/**
	 * Stops the persistent storage
	 * 
	 * @throws StorageException
	 */
	public static void finish() throws StorageException {
		logger.info("[LOG] Storage Finish");

		// Remove WD
		// All nodes may execute this code so we only erase it
		// Worker sandboxes are inside so they are automatically removed
		/*try {
			File wd = new File(BASE_WORKING_DIR);
			if (wd.exists()) {
				FileUtils.deleteDirectory(new File(BASE_WORKING_DIR));
			}
		} catch (IOException e) {
			throw new StorageException(ERROR_ERASE_WD, e);
		}*/
		
		// Log
		logger.info("[LOG] Storage Finish finished");
	}

	/**
	 * Returns all the valid locations of a given id
	 * 
	 * @param id
	 * @return
	 * @throws StorageException
	 */
	public static List<String> getLocations(String id) throws StorageException {
		List<String> result = new LinkedList<String> ();
		
		for (String hostname : hostnames) {
			String path = BASE_WORKING_DIR + hostname + File.separator + id;
			File pscoLocation = new File(path);
			if (pscoLocation.exists()) {
				result.add(hostname);
			}
		}

		return result;
	}

	/**
	 * Creates a new replica of PSCO id @id in host @hostname
	 * 
	 * @param id
	 * @param hostName
	 * @throws StorageException
	 */
	public static void newReplica(String id, String hostName) throws StorageException {
		logger.info("NEW REPLICA: " + id + " on host " + hostName);
		// New replica always copies PSCO from master
		File source = new File(MASTER_WORKING_DIR + id);
		if (source.exists()) {
			String targetPath = BASE_WORKING_DIR + hostName + File.separator + id;
			File target = new File(targetPath);
			try {
				Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new StorageException(ERROR_NEW_REPLICA + id, e);
			}
		} else {
			throw new StorageException(ERROR_NO_PSCO + id);
		}
	}

	/**
	 * Create a new version of the PSCO id @id in the host @hostname
	 * Returns the id of the new version
	 * 
	 * @param id
	 * @param hostName
	 * @return
	 * @throws StorageException
	 */
	public static String newVersion(String id, String hostName) throws StorageException {
		logger.info("NEW VERSION: " + id + " on host " + hostName);
		
		// New version always copies PSCO from master
		File source = new File(MASTER_WORKING_DIR + id);
		String newId = "psco_" + UUID.randomUUID().toString();
		if (source.exists()) {
			String targetPath = MASTER_WORKING_DIR + newId;
			File target = new File(targetPath);
			try {
				Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				throw new StorageException(ERROR_NEW_VERSION + id + " to " + targetPath, e);
			}
		} else {
			throw new StorageException(ERROR_NO_PSCO + id);
		}
		
		return newId;
	}

	/**
	 * Returns the object with id @id
	 * This function retrieves the object from any location
	 * 
	 * @param id
	 * @return
	 * @throws StorageException
	 */
	public static Object getByID(String id) throws StorageException {
		// Retrieves the Object from any worker location
		for (String hostname : hostnames) {
			String path = BASE_WORKING_DIR + hostname + File.separator + id;
			File source = new File(path);
			if (source.exists()) {
				try {
					Object obj = Serializer.deserialize(path);
					return obj;
				} catch (ClassNotFoundException e) {
					throw new StorageException(ERROR_DESERIALIZE + id, e);
				} catch (IOException e) {
					throw new StorageException(ERROR_DESERIALIZE + id, e);
				}
			}
		}
		
		// If we reach this point the ID has not been found.
		throw new StorageException(ERROR_NO_PSCO + id);
	} 

	/**
	 * Executes the task into persistent storage
	 * 
	 * @param id
	 * @param descriptor
	 * @param values
	 * @param hostName
	 * @param callback
	 * @return
	 * @throws StorageException
	 */
	public static String executeTask(String id, String descriptor,
			Object[] values, String hostName, CallbackHandler callback) throws StorageException {
		
		throw new UnsupportedOperationException();
	}

	/**
	 * Retrieves the result of persistent storage execution
	 * 
	 * @param event
	 * @return
	 */
	public static Object getResult(CallbackEvent event) {
		// Nothing to do since executeTask is not supported
		return null;
	}
	
	
	/* ************************************************
	 * SPECIFIC IMPLEMENTATION METHODS
	 * ************************************************/
	/**
	 * Stores the object @o in the persistent storage with id @id
	 * 
	 * @param o
	 * @param id
	 * @throws StorageException
	 */
	public static void makePersistent(Object o, String id) throws StorageException {
		String path = MASTER_WORKING_DIR + id;
		try {
			Serializer.serialize(o, path);
		} catch (IOException e) {
			throw new StorageException(ERROR_SERIALIZE + id, e);
		}
	}
	
	/**
	 * Removes all the occurrences of a given @id
	 * 
	 * @param id
	 */
	public static void removeById(String id) {
		// Retrieves the Object from any worker location
		for (String hostname : hostnames) {
			String path = BASE_WORKING_DIR + hostname + File.separator + id;
			File source = new File(path);
			if (source.exists()) {
				source.delete();
			}
		}
	}

}