package bugsandwich.ornably.account;

import bugsandwich.ornably.address.AddressDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class OnboardSignupRequest {

    private AccountDTO account;
    private AddressDTO address;

}
