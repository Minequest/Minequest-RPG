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
		CallableStatement statement = db.getConnection().prepareCall(PLAYER_ADD);
		statement.setString(1,user);
		statement.registerOutParameter(2, Types.BLOB);

		Blob blob = statement.getBlob(2);
		ObjectOutputStream oop = new ObjectOutputStream(blob.setBinaryStream(1));
		oop.writeObject(params);
		oop.flush();
		oop.close();
		statement.execute();
		statement.close();
	}

	public static void updatePlayerObject(String user, PlayerDetails params) throws SQLException, IOException {
		DatabaseHandler db = MineQuest.sqlstorage.getDB();
		CallableStatement statement = db.getConnection().prepareCall(PLAYER_UPDATE);
		statement.registerOutParameter(1, Types.BLOB);
		statement.setString(2,user);

		Blob blob = statement.getBlob(1);
		ObjectOutputStream oop = new ObjectOutputStream(blob.setBinaryStream(1));
		oop.writeObject(params);
		oop.flush();
		oop.close();
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
