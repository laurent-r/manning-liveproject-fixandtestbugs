package com.assetco.hotspots.optimization;

import com.assetco.search.results.*;

import java.util.*;

import static com.assetco.search.results.AssetVendorRelationshipLevel.*;
import static com.assetco.search.results.HotspotKey.*;

// This code manages filling the showcase if it's not already set
// it make sure the first partner-lvl vendor with enough assets on the page gets the showcase
//
// From Jamie's reqs:
//   1. If a Partner-level vendor has at least three (3) assets in the result set, that partner's assets shall own the showcase
//   2. If two (2) Partner-level vendors meet the criteria to own the showcase, the first vendor to meet the criteria shall own the showcase
//   3. If a Partner-level has more than five (5) showcase assets, additional assets shall be treated as Top Picks
//
// -johnw
// 1/3/07

/**
 * Assigns assets to the showcase hotspot group based on their vendor status.
 */
class RelationshipBasedOptimizer {
    public void optimize(SearchResults searchResults) {
        Iterator<Asset> iterator = searchResults.getFound().iterator();
        // don't affect a showcase built by an earlier rule
        var showcaseFull = searchResults.getHotspot(Showcase).getMembers().size() > 0;
        List<Asset> showcaseAssets = new ArrayList<>();
        var showcaseAssetsByPartner = new HashMap<AssetVendor, List<Asset>>();
        var partnerAssets = new ArrayList<Asset>();
        var goldAssets = new ArrayList<Asset>();
        var silverAssets = new ArrayList<Asset>();

        while (iterator.hasNext()) {
            Asset asset = iterator.next();
            // HACK! trap gold and silver assets for use later
            if (asset.getVendor().getRelationshipLevel() == Gold)
                goldAssets.add(asset);
            else if (asset.getVendor().getRelationshipLevel() == Silver)
                silverAssets.add(asset);

            if (asset.getVendor().getRelationshipLevel() != Partner)
                continue;

            // remember this partner asset
            partnerAssets.add(asset);

            // if we don't have a showcase, get one for this partner
            if (showcaseAssets.isEmpty()) {
                showcaseAssets = feedShowcaseAssetsForPartner(showcaseAssetsByPartner, asset);
            } else if (Objects.equals(showcaseAssets.get(0).getVendor(), asset.getVendor())) {
                if (showcaseAssets.size() >= 5) {
                    searchResults.getHotspot(TopPicks).addMember(asset);
                } else {
                    showcaseAssets = feedShowcaseAssetsForPartner(showcaseAssetsByPartner, asset);
                }
            } else if (showcaseAssets.size() < 3) {
                // switch to showcase for the new target partner
                showcaseAssets = feedShowcaseAssetsForPartner(showcaseAssetsByPartner, asset);
            }
        }

        // [DBV], 4/14/2014:
        // need added this here even though it's not about this rules
        // frm Jamie,
        // 1. All partner assets should be eligible for high-value slots in the main grid.
        // 2. All partner assets should be eligible to appear in the fold.

        // todo - this does not belong here!!!
        var highValueHotspot = searchResults.getHotspot(HighValue);
        for (var asset : partnerAssets)
            if (!highValueHotspot.getMembers().contains(asset))
                highValueHotspot.addMember(asset);

        // TODO - this needs to be moved to something that only manages the fold
        for (var asset : partnerAssets)
            searchResults.getHotspot(Fold).addMember(asset);

    // only copy showcase assets into hotspot if there are enough for a partner to claim the showcase
        if (!showcaseFull && showcaseAssets.size() >= 3) {
            Hotspot showcaseHotspot = searchResults.getHotspot(Showcase);
            for (Asset asset : showcaseAssets)
                showcaseHotspot.addMember(asset);
        }

        // acw-14339: gold assets should be in high value hotspots if there are no partner assets in search
        for (var asset : goldAssets)
            if (!highValueHotspot.getMembers().contains(asset))
                highValueHotspot.addMember(asset);

        // acw-14341: gold assets should appear in fold box when appropriate
        for (var asset : goldAssets)
            searchResults.getHotspot(Fold).addMember(asset);

        // LOL acw-14511: gold assets should appear in fold box when appropriate
        for (var asset : silverAssets)
            searchResults.getHotspot(Fold).addMember(asset);
    }

    private List<Asset> feedShowcaseAssetsForPartner(Map<AssetVendor, List<Asset>> map, Asset asset) {
        map.putIfAbsent(asset.getVendor(), new ArrayList<>());
        List<Asset> showcaseAssets = map.get(asset.getVendor());
        showcaseAssets.add(asset);
        return showcaseAssets;
    }
}
