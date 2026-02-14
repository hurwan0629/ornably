package bugsandwich.ornably.address;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AddressRepository {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	// 주소 전체 삭제 (회원 탈퇴 시)
    private static final String DELETE_ALL_ADDRESS_BY_ACCOUNT_PK =
            "DELETE FROM ADDRESS WHERE ACCOUNT_PK = ?";

    // 주소 한 개 삭제
    private static final String DELETE_ADDRESS_BY_ADDRESS_PK =
            "DELETE FROM ADDRESS WHERE ADDRESS_PK = ?";

    // 회원의 모든 주소 조회
    private static final String SELECT_ALL_ADDRESS_BY_ACCOUNT_PK =
            "SELECT " +
            "  ADDRESS_PK        	AS addressPk, " +
            "  ACCOUNT_PK        	AS accountPk, " +
            "  ADDRESS_NAME      	AS addressName, " +
            "  ADDRESS_IS_DEFAULT 	AS addressIsDefault, " +
            "  ADDRESS_POSTAL_CODE 	AS addressPostalCode, " +
            "  ADDRESS_REGION    	AS addressRegion, " +
            "  ADDRESS_DETAIL    	AS addressDetail " +
            "FROM ADDRESS " +
            "WHERE ACCOUNT_PK = ?";

    // 기본 배송지 조회
    private static final String SELECT_DEFAULT_ADDRESS =
            "SELECT ADDRESS_PK AS addressPk FROM ADDRESS " +
            "WHERE ACCOUNT_PK = ? AND ADDRESS_IS_DEFAULT = 1";
    
    // 특정 주소가 기본 배송지인지 확인
    private static final String SELECT_IS_DEFAULT_ADDRESS_BY_ADDRESS_PK =
            "SELECT ADDRESS_PK AS addressPk FROM ADDRESS " +
            "WHERE ADDRESS_PK = ? AND ADDRESS_IS_DEFAULT = 1";

    // 기본 배송지 해제
    private static final String UPDATE_DEFAULT_ADDRESS_REMOVE =
            "UPDATE ADDRESS SET ADDRESS_IS_DEFAULT = FALSE " +
            "WHERE ACCOUNT_PK = ? AND ADDRESS_IS_DEFAULT = TRUE";

    // 기본 배송지로 설정
    private static final String UPDATE_DEFAULT_ADDRESS =
            "UPDATE ADDRESS SET ADDRESS_IS_DEFAULT = TRUE " +
            "WHERE ADDRESS_PK = ? AND ACCOUNT_PK = ? ";

    // 주소 등록
    private static final String INSERT_NEW_ADDRESS =
            "INSERT INTO ADDRESS " +
            "(ACCOUNT_PK, ADDRESS_NAME, ADDRESS_IS_DEFAULT, ADDRESS_POSTAL_CODE, ADDRESS_REGION, ADDRESS_DETAIL) " +
            "VALUES (?, ?, IFNULL(?, FALSE), ?, ?, ?)";

    
	public List<AddressDTO> selectAll(AddressDTO addressDTO) {
		
		
		// 해당 회원의 모든 주소지 조회
		if ("SELECT_ALL_ADDRESS_BY_ACCOUNT_PK".equals(addressDTO.getCondition())) {
			
			return jdbcTemplate.query(
                SELECT_ALL_ADDRESS_BY_ACCOUNT_PK,
                new BeanPropertyRowMapper<>(AddressDTO.class),
                addressDTO.getAccountPk()
            );
		}
		
		return null;
	}

	public AddressDTO selectOne(AddressDTO addressDTO) {
		
		
		// 해당 회원의 기본주소지 조회
		if ("SELECT_DEFAULT_ADDRESS".equals(addressDTO.getCondition())) {
			
			List<AddressDTO> list = jdbcTemplate.query(SELECT_DEFAULT_ADDRESS,
                (rs, rowNum) -> {
                    AddressDTO data = new AddressDTO();
                    data.setAddressPk(rs.getInt("ADDRESS_PK"));
                    return data;
                },
                addressDTO.getAccountPk()
            );
            return list.isEmpty() ? null : list.get(0);
        }
		
		// 특정 주소가 "기본 배송지인지 여부" 확인
		else if ("SELECT_IS_DEFAULT_ADDRESS_BY_ADDRESS_PK".equals(addressDTO.getCondition())) {
        	
            List<AddressDTO> list = jdbcTemplate.query(SELECT_IS_DEFAULT_ADDRESS_BY_ADDRESS_PK,
                (rs, rowNum) -> {
                    AddressDTO data = new AddressDTO();
                    data.setAddressPk(rs.getInt("ADDRESS_PK"));
                    return data;
                },
                addressDTO.getAddressPk()
            );
            return list.isEmpty() ? null : list.get(0);
        }
		
        return null;
	}

	public boolean insert(AddressDTO addressDTO) {
		
		
		// 새로운 배송지 주소 ADDRESS 테이블에 추가
		if("INSERT_NEW_ADDRESS".equals(addressDTO.getCondition())) {
        	
			return jdbcTemplate.update(
		        INSERT_NEW_ADDRESS,
		        addressDTO.getAccountPk(),
		        addressDTO.getAddressName(),
		        addressDTO.getAddressIsDefault(),
		        addressDTO.getAddressPostalCode(),
		        addressDTO.getAddressRegion(),
		        addressDTO.getAddressDetail()
		    ) > 0;
		}
		
        return false;
	}

	public boolean update(AddressDTO addressDTO) {
		
		
		// 해당 회원의 기본 배송지 해제
        if ("UPDATE_DEFAULT_ADDRESS_REMOVE".equals(addressDTO.getCondition())) {
        	
            return jdbcTemplate.update(
        		UPDATE_DEFAULT_ADDRESS_REMOVE, 
        		addressDTO.getAccountPk()) > 0;
        }
        
        // 특정 주소를 기본 배송지로 설정
        else if (addressDTO.getCondition().equals("UPDATE_DEFAULT_ADDRESS")) {
        	
            return jdbcTemplate.update(
        		UPDATE_DEFAULT_ADDRESS,
        		addressDTO.getAddressPk(),
                addressDTO.getAccountPk()) > 0;
        }
		
        return false;
    }

	public boolean delete(AddressDTO addressDTO) {
		
		
		// 특정 주소 1개 삭제
        if ("DELETE_ADDRESS_BY_ADDRESS_PK".equals(addressDTO.getCondition())) {
    		
            return jdbcTemplate.update(
                DELETE_ADDRESS_BY_ADDRESS_PK,
                addressDTO.getAddressPk()
            ) > 0;
        }
        
        // 회원 탈퇴 시 해당 회원의 모든 주소 삭제
        else if ("DELETE_ALL_ADDRESS_BY_ACCOUNT_PK".equals(addressDTO.getCondition())) {
        	
            return jdbcTemplate.update(
                DELETE_ALL_ADDRESS_BY_ACCOUNT_PK,
                addressDTO.getAccountPk()
            ) > 0;
        }
		
        return false;
    }
}


