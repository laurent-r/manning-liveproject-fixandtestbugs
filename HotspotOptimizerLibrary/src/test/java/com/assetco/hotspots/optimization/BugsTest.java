package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.Showcase;

public class BugsTest {
    @Test
    void precedingPartnerWithLongTrailingAssetsDoesNotWin() {
        AssetVendor partnerVendor = makeVendor(Partner);
        Asset missing = givenAssetInResultsWithVendor(partnerVendor);
        Asset disruptingAsset = givenAssetInResultsWithVendor(makeVendor(Partner));
        var expected = new ArrayList<Asset>();
        expected.add(givenAssetInResultsWithVendor(partnerVendor));
        expected.add(givenAssetInResultsWithVendor(partnerVendor));
        expected.add(givenAssetInResultsWithVendor(partnerVendor));
        expected.add(givenAssetInResultsWithVendor(partnerVendor));

        whenOptimize();

        thenHotspotDoesNotHave(Showcase, missing);
        thenHotspotHasExactly(Showcase, expected);
    }

    private AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        return null;
    }

    private Asset givenAssetInResultsWithVendor(AssetVendor vendor) {
        return null;
    }

    private void whenOptimize() {
    }
    private void thenHotspotDoesNotHave(HotspotKey key, Asset... assets) {
    }

    private void thenHotspotHasExactly(HotspotKey key, List<Asset> assets) {
    }
}
