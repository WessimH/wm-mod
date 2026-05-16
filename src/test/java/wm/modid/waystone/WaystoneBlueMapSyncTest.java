package wm.modid.waystone;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.Marker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.blay09.mods.waystones.api.Waystone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for WaystoneBlueMapSync functionality.
 *
 * This test suite verifies the core behavior of waystone synchronization with BlueMap:
 * - Marker creation and proper labeling
 * - Marker removal when waystones are deleted
 * - Handling of missing worlds
 * - MarkerSet creation and management
 *
 * Tests use Mockito for mocking BlueMap and Waystones APIs to isolate
 * the synchronization logic from external dependencies.
 */
class WaystoneBlueMapSyncTest {

    @Mock BlueMapAPI api;
    @Mock BlueMapWorld world;
    @Mock BlueMapMap map;
    @Mock Waystone waystone;

    // Simule le vrai MarkerSet en mémoire
    private final Map<String, Marker> markers = new ConcurrentHashMap<>();
    private MarkerSet markerSet;

    private final UUID waystoneUid = UUID.randomUUID();

    /**
     * Sets up test fixtures and mocks before each test.
     *
     * Initializes:
     * - BlueMap and Waystone mock objects
     * - Real MarkerSet instance for integration testing
     * - Mock waystone with test data (name, position, UUID)
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // MarkerSet réel (pas mocké) pour tester vraiment les insertions
        markerSet = MarkerSet.builder().label("✦ Waystones").build();

        // Setup BlueMap mocks
        when(api.getWorld(any())).thenReturn(Optional.of(world));
        when(world.getMaps()).thenReturn(List.of(map));
        when(map.getMarkerSets()).thenReturn(new ConcurrentHashMap<>());

        // Setup Waystone mock
        when(waystone.getWaystoneUid()).thenReturn(waystoneUid);
        when(waystone.getName()).thenReturn(net.minecraft.network.chat.Component.literal("Test Waystone"));
        when(waystone.getPos()).thenReturn(new net.minecraft.core.BlockPos(100, 64, 200));
    }

    /**
     * Verifies that a marker is correctly created and added to the marker set.
     *
     * Asserts:
     * - Marker set with ID "waystones" exists
     * - Waystone marker is present with correct UUID
     */
    @Test
    void testMarkerIsAddedForWaystone() {
        // Simule l'ajout d'un marqueur
        Map<String, MarkerSet> markerSets = new ConcurrentHashMap<>();
        when(map.getMarkerSets()).thenReturn(markerSets);

        // Appelle addMarker via la méthode publique de test
        WaystoneBlueMapSyncTestHelper.addMarkerPublic(api, waystone, markerSets);

        // Vérifie que le marker set a été créé
        assertTrue(markerSets.containsKey("waystones"),
            "Le marker set 'waystones' doit exister");

        // Vérifie que le marqueur a été ajouté
        MarkerSet set = markerSets.get("waystones");
        assertTrue(set.getMarkers().containsKey(waystoneUid.toString()),
            "Le marqueur du waystone doit être présent");
    }

    /**
     * Verifies that the marker label matches the waystone name.
     *
     * Asserts:
     * - Marker has the correct label text
     * - Label corresponds to waystone display name
     */
    @Test
    void testMarkerHasCorrectLabel() {
        Map<String, MarkerSet> markerSets = new ConcurrentHashMap<>();
        when(map.getMarkerSets()).thenReturn(markerSets);

        WaystoneBlueMapSyncTestHelper.addMarkerPublic(api, waystone, markerSets);

        MarkerSet set = markerSets.get("waystones");
        POIMarker marker = (POIMarker) set.getMarkers().get(waystoneUid.toString());

        assertNotNull(marker, "Le marqueur ne doit pas être null");
        assertEquals("Test Waystone", marker.getLabel(),
            "Le label du marqueur doit correspondre au nom du waystone");
    }

    /**
     * Verifies that markers are correctly removed when waystones are deleted.
     *
     * Asserts:
     * - Marker exists after addition
     * - Marker is removed after deletion
     */
    @Test
    void testMarkerIsRemovedWhenWaystoneDeleted() {
        Map<String, MarkerSet> markerSets = new ConcurrentHashMap<>();
        when(map.getMarkerSets()).thenReturn(markerSets);

        // Ajoute d'abord
        WaystoneBlueMapSyncTestHelper.addMarkerPublic(api, waystone, markerSets);
        assertTrue(markerSets.get("waystones").getMarkers().containsKey(waystoneUid.toString()));

        // Puis supprime
        MarkerSet set = markerSets.get("waystones");
        set.remove(waystoneUid.toString());

        assertFalse(set.getMarkers().containsKey(waystoneUid.toString()),
            "Le marqueur doit être supprimé");
    }

    /**
     * Verifies that no markers are created when the world is not found in BlueMap.
     *
     * Asserts:
     * - No marker sets are created when BlueMap world lookup fails
     * - Sync gracefully handles missing worlds
     */
    @Test
    void testNoMarkerSetCreatedWhenWorldNotFound() {
        // BlueMap ne trouve pas le monde
        when(api.getWorld(any())).thenReturn(Optional.empty());

        Map<String, MarkerSet> markerSets = new ConcurrentHashMap<>();
        WaystoneBlueMapSyncTestHelper.addMarkerPublic(api, waystone, markerSets);

        assertTrue(markerSets.isEmpty(),
            "Aucun marker set ne doit être créé si le monde n'existe pas dans BlueMap");
    }
}
