/**
 * -----------------------------------------------------------------------
 *     Copyright  2010 ShepHertz Technologies Pvt Ltd. All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.shephertz.app42.push.plugin;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The Class PushMessage.
 *
 * @author Vishnu
 */

public class PushMessage {
	
	/** The push type. */
	private PushType pushType=PushType.Simple;
	
	/** The title. */
	private String title;
	
	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/** The message. */
	private String message;
	
	/** The extra data. */
	private HashMap<String, Object> extraData;
	
	/** The push camp identifer. */
	private String pushCampIdentifer;
	
	/** The geo push list. */
	private ArrayList<GeoPush> geoPushList;
	
	/**
	 * Gets the geo push list.
	 *
	 * @return the geo push list
	 */
	public ArrayList<GeoPush> getGeoPushList() {
		return geoPushList;
	}
	
	/** The rich push. */
	private RichPush richPush;
	
	/** The is geo push. */
	private boolean isGeoPush=false;

	/**
	 * Checks if is geo push.
	 *
	 * @return true, if is geo push
	 */
	public boolean isGeoPush() {
		return isGeoPush;
	}
	
	/**
	 * Gets the rich push json.
	 *
	 * @return the rich push json
	 * @throws JSONException the JSON exception
	 */
	public JSONObject getRichPushJson() throws JSONException {
		JSONObject jsonMessage=new JSONObject();
		jsonMessage.put(App42Util.KeyType, richPush.getRichType().toString());
		jsonMessage.put(App42Util.KeyContent, richPush.getContent());
		return jsonMessage;
	}
	
	/**
	 * Gets the rich push.
	 *
	 * @return the rich push
	 */
	public RichPush getRichPush() {
		return richPush;
	}

	/**
	 * Sets the geo location push.
	 *
	 * @param lattitude the lattitude
	 * @param longtitude the longtitude
	 * @param radius the radius
	 * @param geoType the geo type
	 */
	public void setGeoLocationPush(double lattitude, double longtitude, double radius,
			GeoType geoType) {
		if(geoPushList==null)
			geoPushList=new ArrayList<PushMessage.GeoPush>();
		geoPushList.add(new GeoPush(lattitude, longtitude, radius, geoType));
		isGeoPush=true;
	}
	
	/**
	 * Sets the geo address push.
	 *
	 * @param country the country
	 * @param state the state
	 * @param city the city
	 * @param geoType the geo type
	 */
	public void setGeoAddressPush(String country, String state, String city,
			GeoType geoType) {
		if(geoPushList==null)
			geoPushList=new ArrayList<PushMessage.GeoPush>();
		geoPushList.add(new GeoPush(country, state, city, geoType));
		isGeoPush=true;
	}
	
	/**
	 * Sets the rich push.
	 *
	 * @param type the type
	 * @param content the content
	 */
	public void setRichPush(String type,String content) {
		if(type.equalsIgnoreCase("youtube")||type.equalsIgnoreCase("youTubeVideo"))
			this.richPush = new RichPush(RichType.youtube, content);
		else if(type.equalsIgnoreCase("openUrl")){
			this.richPush = new RichPush(RichType.OpenUrl, content);
		}
		else
		this.richPush = new RichPush(RichType.valueOf(type), content);
		this.pushType=PushType.Rich;
	}

	/**
	 * Instantiates a new push message.
	 *
	 * @param message the message
	 * @param title the title
	 */
	public PushMessage(String message, String title) {
		this.message = message;
		this.title = title;
	}

	/**
	 * Gets the push type.
	 *
	 * @return the push type
	 */
	public PushType getPushType() {
		return pushType;
	}

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Gets the extra data.
	 *
	 * @return the extra data
	 */
	public HashMap<String, Object> getExtraData() {
		return extraData;
	}

	/**
	 * Gets the push camp identifer.
	 *
	 * @return the push camp identifer
	 */
	public String getPushCampIdentifer() {
		return pushCampIdentifer;
	}

	/**
	 * Sets the push type.
	 *
	 * @param pushType the new push type
	 */
	public void setPushType(PushType pushType) {
		this.pushType = pushType;
	}

	/**
	 * Sets the extra data.
	 *
	 * @param extraData the extra data
	 */
	public void setExtraData(HashMap<String, Object> extraData) {
		this.extraData = extraData;
	}

	/**
	 * Sets the push camp identifer.
	 *
	 * @param pushCampIdentifer the new push camp identifer
	 */
	public void setPushCampIdentifer(String pushCampIdentifer) {
		this.pushCampIdentifer = pushCampIdentifer;
	}

	/**
	 * The Class GeoPush.
	 *
	 * @author Vishnu
	 */
	public class GeoPush {
		
		/** The geo type. */
		private GeoType geoType;
		
		/** The lattitude. */
		private double lattitude;
		
		/** The longtitude. */
		private double longtitude;
		
		/** The radius. */
		private double radius;
		
		/** The country. */
		private String country;
		
		/** The state. */
		private String state;
		
		/** The city. */
		private String city;
		
		/** The content avail. */
		private int contentAvail;

		/**
		 * Gets the content avail.
		 *
		 * @return the content avail
		 */
		public int getContentAvail() {
			return contentAvail;
		}

		/**
		 * Sets the content avail.
		 *
		 * @param contentAvail the new content avail
		 */
		public void setContentAvail(int contentAvail) {
			this.contentAvail = contentAvail;
		}

		/**
		 * Gets the geo type.
		 *
		 * @return the geo type
		 */
		public GeoType getGeoType() {
			return geoType;
		}

		/**
		 * Gets the lattitude.
		 *
		 * @return the lattitude
		 */
		public double getLattitude() {
			return lattitude;
		}

		/**
		 * Gets the longtitude.
		 *
		 * @return the longtitude
		 */
		public double getLongtitude() {
			return longtitude;
		}

		/**
		 * Gets the radius.
		 *
		 * @return the radius
		 */
		public double getRadius() {
			return radius;
		}

		/**
		 * Gets the country.
		 *
		 * @return the country
		 */
		public String getCountry() {
			return country;
		}

		/**
		 * Gets the state.
		 *
		 * @return the state
		 */
		public String getState() {
			return state;
		}

		/**
		 * Gets the city.
		 *
		 * @return the city
		 */
		public String getCity() {
			return city;
		}

		/**
		 * Instantiates a new geo push.
		 *
		 * @param lattitude the lattitude
		 * @param longtitude the longtitude
		 * @param radius the radius
		 * @param geoType the geo type
		 */
		public GeoPush(double lattitude, double longtitude, double radius,
				GeoType geoType) {
			this.lattitude = lattitude;
			this.longtitude = longtitude;
			this.radius = radius;
			this.geoType = geoType;
		}

		/**
		 * Instantiates a new geo push.
		 *
		 * @param country the country
		 * @param state the state
		 * @param city the city
		 * @param geoType the geo type
		 */
		public GeoPush(String country, String state, String city,
				GeoType geoType) {
			this.country = country;
			this.city = city;
			this.state = state;
			this.geoType = geoType;
		}

	}

	/**
	 * The Class RichPush.
	 *
	 * @author Vishnu
	 */
	public class RichPush {
		
		/** The rich type. */
		private RichType richType;
		
		/** The content. */
		private String content;

		/**
		 * Gets the rich type.
		 *
		 * @return the rich type
		 */
		public RichType getRichType() {
			return richType;
		}

		/**
		 * Gets the content.
		 *
		 * @return the content
		 */
		public String getContent() {
			return content;
		}

		/**
		 * Instantiates a new rich push.
		 *
		 * @param richType the rich type
		 * @param content the content
		 */
		public RichPush(RichType richType, String content) {
			super();
			this.richType = richType;
			this.content = content;
		}
	}

	/**
	 * The Enum PushType.
	 *
	 * @author Vishnu
	 */
	public enum PushType {
		 
 		/** The Rich. */
 		Rich, 
 /** The Simple. */
 Simple;
	}

	/**
	 * The Enum GeoType.
	 */
	public enum GeoType {
		
		/** The Address based. */
		AddressBased, 
 /** The Location based. */
 LocationBased;
	}

	/**
	 * The Enum RichType.
	 */
	public enum RichType {
		
		/** The text. */
		text, 
 /** The image. */
 image, 
 /** The video. */
 video, 
 /** The Open url. */
 OpenUrl, 
 /** The html. */
 html, 
 /** The youtube. */
 youtube;
	}
}
