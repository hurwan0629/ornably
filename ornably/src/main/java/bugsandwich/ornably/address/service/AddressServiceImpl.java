package bugsandwich.ornably.address.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import bugsandwich.ornably.address.AddressDTO;
import bugsandwich.ornably.address.AddressRepository;

@Service
public class AddressServiceImpl implements AddressService{
	
	@Autowired // DB의존성 주입
	private AddressRepository addressRepository;

	@Override
	public boolean insertAddress(AddressDTO addressDTO) {
		return addressRepository.insert(addressDTO);
	}

	@Override
	public boolean updateAddress(AddressDTO addressDTO) {
		return addressRepository.update(addressDTO);
	}

	@Override
	public boolean deleteAddress(AddressDTO addressDTO) {
		return addressRepository.delete(addressDTO);
	}

	@Override
	public AddressDTO getAddress(AddressDTO addressDTO) {	
		return addressRepository.selectOne(addressDTO);
	}

	@Override
	public List<AddressDTO> getAddressList(AddressDTO addressDTO) {
		return addressRepository.selectAll(addressDTO);
	}

	@Override
	public boolean changeDefaultAddress(AddressDTO addressDTO) {
		
		addressDTO.setCondition("UPDATE_DEFAULT_ADDRESS_REMOVE");
		this.addressRepository.update(addressDTO);
		
		addressDTO.setCondition("UPDATE_DEFAULT_ADDRESS");
		this.addressRepository.update(addressDTO);
		
		return true;
	}

	

}
