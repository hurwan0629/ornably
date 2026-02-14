package bugsandwich.ornably.connectLog;

import java.util.List;


public interface ConnectLogService {
	boolean insertMember(ConnectLogDTO dto);
	boolean updateMember(ConnectLogDTO dto);
	boolean deleteMember(ConnectLogDTO dto);
	
	ConnectLogDTO getMember(ConnectLogDTO dto);
	List<ConnectLogDTO> getMemberList(ConnectLogDTO dto);
}
