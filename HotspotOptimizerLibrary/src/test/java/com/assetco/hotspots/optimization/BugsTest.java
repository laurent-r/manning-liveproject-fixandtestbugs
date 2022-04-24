package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.Showcase;
import static org.junit.jupiter.api.Assertions.*;

public class BugsTest {
    private SearchResults results;
    private SearchResultHotspotOptimizer optimizer;

    @BeforeEach
    public void setup() {
        results = new SearchResults();
        optimizer = new SearchResultHotspotOptimizer();
    }

    @Test
    void partnerWithLongTrailingAssetsWins() {
        AssetVendor partner1 = makeVendor(Partner);
        AssetVendor partner2 = makeVendor(Partner);
        var expected = new ArrayList<Asset>();
        expected.add(givenAssetInResultsWithVendor(partner1));
        var unexpected1 = givenAssetInResultsWithVendor(partner2);
        expected.add(givenAssetInResultsWithVendor(partner1));
        expected.add(givenAssetInResultsWithVendor(partner1));
        var unexpected2 = givenAssetInResultsWithVendor(partner2);
        expected.add(givenAssetInResultsWithVendor(partner1));
        expected.add(givenAssetInResultsWithVendor(partner1));

        whenOptimize();

        thenHotspotDoesNotHave(Showcase, unexpected1, unexpected2);
        thenHotspotHasExactly(Showcase, expected);
    }

    private AssetVendor makeVendor(AssetVendorRelationshipLevel relationshipLevel) {
        var id = Any.string();
        var displayName = Any.string();
        long royaltyRate = Any.anyLong();

        var vendor = new AssetVendor(id, displayName, relationshipLevel, royaltyRate);

        return vendor;
    }

    private Asset givenAssetInResultsWithVendor(AssetVendor vendor) {
        var id = Any.string();
        var title = Any.string();
        var thumbnailURI = Any.URI();
        var previewURI = Any.URI();
        var last30Days = Any.assetPurchaseInfo();
        var last24Hours = Any.assetPurchaseInfo();
        var topics = Any.setOfTopics();

        var asset = new Asset(id, title, thumbnailURI, previewURI, last30Days, last24Hours, topics, vendor);
        results.addFound(asset);

        return asset;
    }

    private void whenOptimize() {
        optimizer.optimize(results);
    }
    private void thenHotspotDoesNotHave(HotspotKey key, Asset... assets) {
        for (Asset asset : assets) {
            assertFalse(results.getHotspot(key).getMembers().contains(asset));
        }
    }

    private void thenHotspotHasExactly(HotspotKey key, List<Asset> assets) {
        assertArrayEquals(assets.toArray(), results.getHotspot(key).getMembers().toArray());
    }
}
