package bfst21.address;

import bfst21.osm.Node;
import bfst21.osm.OsmAddress;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TriesMap implements Serializable {

    private static final long serialVersionUID = 776037726558315184L;

    HashMap<AddressType, TST<Node>> triesMap = new HashMap<>();

    public TriesMap() {
        for (AddressType addressType : AddressType.values()) {
            triesMap.put(addressType, new TST<>());
        }
    }

    public void addAddress(OsmAddress osmAddress) {

        for (AddressType addressType : AddressType.values()) {
            TST<Node> tries = triesMap.get(addressType);

            if (addressType == AddressType.FULL) {
                tries.put(osmAddress.toString(), osmAddress.getNode());

            } else if (addressType == AddressType.STREET) {
                tries.put(osmAddress.getStreet(), osmAddress.getNode());

            } else if (addressType == AddressType.CITY) {
                tries.put(osmAddress.getCity(), osmAddress.getNode());
            }
            triesMap.put(addressType, tries);
        }
    }

    public List<String> keysWithPrefix(String prefix, int textLength) {
        List<String> suggestions;

        //Give suggestions for FULL addresses if long search text length
        if (textLength >= 20) {
            suggestions = getSuggestions(prefix, AddressType.FULL);
        } else {
            suggestions = getSuggestions(prefix, AddressType.STREET);

            //Give suggestions for FULL addresses when there is only a few suggestions
            if (suggestions.size() <= 5) {
                suggestions = getSuggestions(prefix, AddressType.FULL);
            }
        }
        if (suggestions.size() > 10) {
            return suggestions.subList(0, 10);
        }
        return suggestions;
    }

    private List<String> getSuggestions(String prefix, AddressType addressType) {
        List<String> suggestions = new ArrayList<>();

        Iterable<String> it = triesMap.get(addressType).keysWithPrefix(prefix);

        for (String suggestion : it) {
            suggestions.add(suggestion);
        }
        return suggestions;
    }

    public Node getNode(String address) {
        return triesMap.get(AddressType.FULL).get(address);
    }
}
