package bugsandwich.ornably.connectLog.service;

import java.util.List;

import bugsandwich.ornably.connectLog.ConnectLogDTO;


public interface ConnectLogService {
	boolean insertConnectLog(ConnectLogDTO connectLogDTO);
	boolean updateConnectLog(ConnectLogDTO connectLog);
	boolean deleteConnectLog(ConnectLogDTO connectLogDTO);
	
	ConnectLogDTO getWishlist(ConnectLogDTO connectLogDTO);
	List<ConnectLogDTO> getWishlistList(ConnectLogDTO connectLogDTO);
	
}
