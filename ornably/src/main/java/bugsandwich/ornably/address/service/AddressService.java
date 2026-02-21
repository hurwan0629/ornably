package bugsandwich.ornably.address.service;

import java.util.List;

import bugsandwich.ornably.address.AddressDTO;

public interface AddressService {

		boolean insertAddress(AddressDTO addressDTO);
		boolean updateAddress(AddressDTO addressDTO);
		boolean deleteAddress(AddressDTO addressDTO);
		
		AddressDTO getAddress(AddressDTO addressDTO);
		List<AddressDTO> getAddressList(AddressDTO addressDTO);
				
		boolean changeDefaultAddress(AddressDTO addressDTO);
}