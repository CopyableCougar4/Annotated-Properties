package com.digiturtle.library.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Core API for handling annotated properties
 */
public class Properties {

	/**
	 * Output stream where all of the values are written
	 */
	private ObjectOutputStream output;
	
	/**
	 * Input stream where all of the values are read from
	 */
	private ObjectInputStream input;
	
	/**
	 * Filename of these properties
	 */
	private String filename;

	/**
	 * Construct a properties object in the default location
	 * @param toRead If true, then you are loading properties, otherwise, you are saving them
	 */
	public Properties(boolean toRead) {
		setupOutput("default", toRead);
	}

	/**
	 * Construct a properties object with a specific name for this set of properties
	 * @param name Name of this set
	 */
	public Properties(String name, boolean toRead) {
		setupOutput(name, toRead);
	}

	/**
	 * Load all annotation values in this object
	 * @param instance Object to look through
	 */
	public void load(Object instance) {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		readProperties(properties);
		Field[] fields = instance.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Property.class)) {
				Property property = field.getAnnotation(Property.class);
				String name = field.getName();
				if (property.alias().length() > 0) {
					name = property.alias();
				}
				try {
					field.set(instance, properties.get(name));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	/**
	 * Save all annotation values in this object
	 * @param instance Object to look through
	 */
	public void save(Object instance) {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		Field[] fields = instance.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(Property.class)) {
				Property property = field.getAnnotation(Property.class);
				String name = field.getName();
				Object object = null;
				try {
					object = field.get(instance);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				if (property.alias().length() > 0) {
					name = property.alias();
				}
				properties.put(name, object);
			}
		}
		writeProperties(properties);
	}
	
	// Internal Methods

	private void setupOutput(String fileName, boolean toRead) {
		String location = "AnnotatedProperties";
		File file = new File(location);
		if (!file.exists()) {
			file.mkdirs();
		}
		location += File.separator + fileName + ".properties";
		try {
			if (!toRead) {
				output = new ObjectOutputStream(new FileOutputStream(location));
			} else {
				if (new File(location).exists()) {
					input = new ObjectInputStream(new FileInputStream(location));
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		filename = location;
	}
	
	private void readProperties(HashMap<String, Object> properties) {
		if (input == null) {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				output = null;
			}
			setupOutput(filename, true);
		}
		try {
			int size = input.readInt();
			for (int index = 0; index < size; index++) {
				properties.put(input.readUTF(), input.readObject());
			}
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private void writeProperties(HashMap<String, Object> properties) {
		if (output == null) {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				input = null;
			}
			setupOutput(filename, false);
		}
		try {
			output.writeInt(properties.size());
			for (Map.Entry<String, Object> entry : properties.entrySet()) {
				output.writeUTF(entry.getKey());
				output.writeObject(entry.getValue());
			}
			output.flush();
			output.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
