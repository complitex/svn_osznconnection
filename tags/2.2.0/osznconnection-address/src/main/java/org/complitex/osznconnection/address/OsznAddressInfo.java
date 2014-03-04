/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.osznconnection.address;

import com.google.common.collect.ImmutableList;

import java.util.List;
import javax.ejb.Singleton;
import org.complitex.address.AddressInfo;
import org.complitex.address.AddressInfoProvider;

/**
 *
 * @author Artem
 */
@Singleton(name = AddressInfoProvider.ADDRESS_INFO_BEAN_NAME)
public class OsznAddressInfo implements AddressInfo {

    private static final List<String> ADDRESSES = ImmutableList.of("country", "region", "city", "city_type", "district", "street", "street_type",
            "building");
    private static final List<String> ADDRESS_DESCRIPTIONS = ImmutableList.of("country", "region", "city", "district", "street", "building");

    @Override
    public List<String> getAddresses() {
        return ADDRESSES;
    }

    @Override
    public List<String> getAddressDescriptions() {
        return ADDRESS_DESCRIPTIONS;
    }
}
