package com.theminequest.MQCoreRPG.Player;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import lib.PatPeter.SQLibrary.DatabaseHandler;

import com.theminequest.MineQuest.MineQuest;

public class PlayerSQL {

	private static final String PLAYER_ADD = "INSERT INTO mq_player VALUES (?, ?)";
	private static final String PLAYER_UPDATE = "UPDATE mq_player SET object = ? WHERE P_NAME = ?";
	private static final String PLAYER_RETRIEVE = "SELECT object FROM mq_player WHERE P_NAME = ?";

	public static void insertPlayerObject(String user, PlayerDetails params) throws SQLException, IOException {
		DatabaseHandler db = MineQuest.sqlstorage.getDB();
		PreparedStatement statement = db.getConnection().prepareStatement(PLAYER_ADD);
		statement.setString(1,user);
		statement.setObject(2, params, java.sql.Types.BLOB);
		statement.execute();
		statement.close();
	}

	public static void updatePlayerObject(String user, PlayerDetails params) throws SQLException, IOException {
		DatabaseHandler db = MineQuest.sqlstorage.getDB();
		PreparedStatement statement = db.getConnection().prepareStatement(PLAYER_UPDATE);
		statement.setString(2,user);
		statement.setObject(1, params, java.sql.Types.BLOB);
		statement.execute();
		statement.close();
	}

	public static PlayerDetails retrievePlayerObject(String user) throws SQLException, IOException {
		DatabaseHandler db = MineQuest.sqlstorage.getDB();
		PreparedStatement statement = db.getConnection().prepareStatement(PLAYER_RETRIEVE);
		statement.setString(1,user);
		ResultSet s = statement.executeQuery();
		if (!s.next())
			return null;
		Blob b = s.getBlob(1);
		ObjectInputStream is = new ObjectInputStream(b.getBinaryStream());
		PlayerDetails d;
		try {
			d = (PlayerDetails) is.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		is.close();
		s.close();
		statement.close();
		return d;
	}

}
