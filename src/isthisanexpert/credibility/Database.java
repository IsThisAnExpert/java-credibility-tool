package isthisanexpert.credibility;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import isthisanexpert.credibility.model.CredibilityScore;
import isthisanexpert.credibility.model.Retweet;
import isthisanexpert.credibility.model.Tweet;
import isthisanexpert.credibility.model.User;

public class Database {

	private static final File dbFile = new File("db.properties");
	
	private static Connection connection = null;
	
	public static Connection getConnectionInstance() {
		return connection;
	}

	public Database()
	{
        try {
        	if( connection == null) {
        		if(!dbFile.exists())
        		{
        			throw new IOException("File db.properties does not exist!");
        		}
        		Properties dbProperties = new Properties();
        		FileInputStream fis = new FileInputStream(dbFile);
        		
        		dbProperties.load(fis);
        		String host = dbProperties.getProperty("host");
        		String dbName = dbProperties.getProperty("dbname");
        		String user = dbProperties.getProperty("user");
        		String password = dbProperties.getProperty("password");
        		String url = "jdbc:mysql://" + host + ":3306/" + dbName;
        		connection = DriverManager.getConnection(url, user, password);
        	}
        }
        catch (Exception e) {
        	e.printStackTrace();
        	close();
        	// cannot do anything without database access
        	System.exit(-1);
        }
	}
	
	public PreparedStatement prepareStatement(String sql) {
		try {
			return connection.prepareStatement(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public User getUser(BigDecimal id) {
		PreparedStatement preparedStatement = prepareStatement(
				"SELECT * FROM hackathon.user WHERE id = ?");
    	try {
	    	preparedStatement.setBigDecimal(1, id);
	        ResultSet resultSet = preparedStatement.executeQuery();
	        resultSet.next();
	        User user = new User(resultSet.getBigDecimal("id"));
	        user.setName(resultSet.getString("name"));
	        //user.setTimeCreated(resultSet.getTimestamp("created_at"));
	        // URL
			return user;
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return null;
	}
	
	public User getUserByName(String username) {
		PreparedStatement preparedStatement = prepareStatement(
				"SELECT * FROM hackathon.user WHERE name LIKE ?");
    	try {
	    	preparedStatement.setString(1, username);
	        ResultSet resultSet = preparedStatement.executeQuery();
	        if(resultSet.next()) {
	        	User user = new User(resultSet.getBigDecimal("id"));
	        	user.setName(resultSet.getString("name"));
	        	//user.setTimeCreated(resultSet.getTimestamp("created_at"));
	        	// URL
	        	return user;	        	
	        }
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return null;
	}
	
	public void insertCredibilityScore(BigDecimal userId, float score, boolean calculated) {
		String type = calculated ? "calculated" : "h-index";
		PreparedStatement preparedStatement = prepareStatement(
				"INSERT INTO hackathon.credibility (score, type, user_id, created_at) VALUES (?, '" + type + "', ?, ?)");
		try {
			preparedStatement.setFloat(1, score);
			preparedStatement.setBigDecimal(2, userId);
			preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			preparedStatement.executeUpdate();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public CredibilityScore getCredibilityScoreByUser(User user) {
		PreparedStatement preparedStatement = prepareStatement(
				"SELECT * FROM hackathon.credibility WHERE user_id LIKE ?");
    	try {
	    	preparedStatement.setBigDecimal(1, user.getId());
	        ResultSet resultSet = preparedStatement.executeQuery();
	        if(resultSet.next()) {
	        	CredibilityScore score = new CredibilityScore(resultSet.getBigDecimal("id"));
	        	score.setScore(resultSet.getFloat("score"));
	        	score.setUser(getUser(resultSet.getBigDecimal("user_id")));
	        	return score;	        	
	        }
	        
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return null;
	}
	
	public CredibilityScore getCredibilityScore(BigDecimal id) {
		PreparedStatement preparedStatement = prepareStatement(
				"SELECT * FROM hackathon.credibility WHERE id = ?");
    	try {
	    	preparedStatement.setBigDecimal(1, id);
	        ResultSet resultSet = preparedStatement.executeQuery();
	        resultSet.next();
	        
	        CredibilityScore score = new CredibilityScore(id);
	        score.setScore(resultSet.getFloat("score"));
	        score.setUser(getUser(resultSet.getBigDecimal("user_id")));
			return score;
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return null;
	}
	
	public List<Retweet> getAllRetweets(User user) {
		ArrayList<Retweet> listOfRetweets = new ArrayList<>();
		PreparedStatement preparedStatement = prepareStatement(
				"SELECT * FROM hackathon.retweet WHERE retweeted_by = ?");
    	try {
	    	preparedStatement.setBigDecimal(1, user.getId());
	        ResultSet resultSet = preparedStatement.executeQuery();
	        while(resultSet.next()) {
	        	Retweet retweet = new Retweet(resultSet.getBigDecimal("id"));
		        retweet.setSource(getTweet(resultSet.getBigDecimal("source_tweet_id")));
		        retweet.setUser(user);
		        Tweet tweet = getTweet(resultSet.getBigDecimal("id"));
		        if(tweet != null) {
		        	retweet.setUserScreenName(tweet.getUserScreenName());
		        	retweet.setContent(tweet.getContent());
		        	retweet.setType(tweet.getType());
		        	// retweet.setTimePosted(
		        	listOfRetweets.add(retweet);
		        }
	        }
			return listOfRetweets;
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return null;
	}
	
	public List<Retweet> getRetweet(BigDecimal id) {
		PreparedStatement preparedStatement = prepareStatement(
				"SELECT * FROM hackathon.retweet WHERE id = ?");
    	try {
	    	preparedStatement.setBigDecimal(1, id);
	        ResultSet resultSet = preparedStatement.executeQuery();
	        resultSet.next();
        	Retweet retweet = new Retweet(id);
	        retweet.setSource(getTweet(resultSet.getBigDecimal("source_tweet_id")));
	        retweet.setUser(getUser(resultSet.getBigDecimal("retweeted_by")));
	        Tweet tweet = getTweet(id);
	        retweet.setUserScreenName(tweet.getUserScreenName());
	        retweet.setContent(tweet.getContent());
	        retweet.setType(tweet.getType());
	        // retweet.setTimePosted(
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return null;
	}
	
	public List<Tweet> getTweets(User user, boolean skipRetweets) {
		ArrayList<Tweet> listOfTweets = new ArrayList<>();
		String sql = "SELECT * FROM hackathon.tweet WHERE user_id = ?";
		if(skipRetweets) {
			sql = "SELECT * FROM hackathon.tweet WHERE user_id = ? AND type = 0";
		}
		PreparedStatement preparedStatement = prepareStatement(sql);
    	try {
	    	preparedStatement.setBigDecimal(1, user.getId());
	        ResultSet resultSet = preparedStatement.executeQuery();
	        while(resultSet.next()) {
	        	Tweet tweet = new Tweet(resultSet.getBigDecimal("id"));
		        tweet.setUser(user);
		        tweet.setUserScreenName(resultSet.getString("user_screen_name"));
		        tweet.setContent(resultSet.getString("content"));
		        tweet.setType(resultSet.getString("type"));
		        // tweet.setTimePosted(resultSet.getTimestamp("posted_time"));
		        listOfTweets.add(tweet);
	        }
			return listOfTweets;
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return null;
	}
	
	public Tweet getTweet(BigDecimal id) {
		PreparedStatement preparedStatement = prepareStatement(
				"SELECT * FROM hackathon.tweet WHERE id = ?");
    	try {
	    	preparedStatement.setBigDecimal(1, id);
	        ResultSet resultSet = preparedStatement.executeQuery();
	        if(resultSet.next()) {
	        	Tweet tweet = new Tweet(id);
	        	tweet.setUser(getUser(resultSet.getBigDecimal("user_id")));
	        	tweet.setUserScreenName(resultSet.getString("user_screen_name"));
	        	tweet.setContent(resultSet.getString("content"));
	        	tweet.setType(resultSet.getString("type"));
	        	// tweet.setTimePosted(resultSet.getTimestamp("posted_time"));
	        	return tweet;
	        }
	        
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return null;
	}
	
	public CredibilityScore findCredibilityScoreForUser(User user) {
		PreparedStatement preparedStatement = prepareStatement(
				"SELECT * FROM hackathon.credibility WHERE user_id = ?");
    	try {
	    	preparedStatement.setBigDecimal(1, user.getId());
	        ResultSet resultSet = preparedStatement.executeQuery();
	        if(resultSet.next()) {
	        	CredibilityScore score = new CredibilityScore(resultSet.getBigDecimal("id"));
	        	score.setScore(resultSet.getFloat("score"));
		        score.setUser(user);
	        } else {
	        	return null;
	        }
    	} catch (SQLException e) {
    		e.printStackTrace();
		}
    	return null;
	}
	
	public static void close() {
		if(connection != null) {
        	try {
				connection.close();
			} catch (SQLException e) {
				// ignore
			}
        }
	}
}
