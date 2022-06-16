package org.nrg.xnatx.plugins.pixi.hotelsplitter.services.impl.xft;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.model.PixiHotelI;
import org.nrg.xdat.model.PixiHotelpositionI;
import org.nrg.xdat.om.PixiHotel;
import org.nrg.xft.ItemI;
import org.nrg.xft.XFTItem;
import org.nrg.xft.event.EventUtils;
import org.nrg.xft.security.UserI;
import org.nrg.xft.utils.SaveItemHelper;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.exceptions.HotelReferenceException;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.exceptions.PixiRuntimeException;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.services.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class XFTHotelService implements HotelService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    private static final String PARAM_HOTEL_NAME        = "name";
    private static final String QUERY_HOTEL_NAME_EXISTS = "SELECT EXISTS(SELECT name FROM pixi_hotel WHERE name = :name)";
    private static final String QUERY_HOTEL_NAME_REFERENCED_BY_HOTEL_SCAN_RECORD = "SELECT EXISTS(SELECT hotel_name FROM pixi_hotelscanrecord WHERE hotel_name = :name)";

    @Autowired
    public XFTHotelService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<PixiHotelI> findAll(final @Nonnull UserI user) {
        return new ArrayList<>(PixiHotel.getAllPixiHotels(user, false));
    }

    @Override
    public Optional<PixiHotelI> findByName(final @Nonnull UserI user, final @Nonnull String name) {
        return Optional.ofNullable(PixiHotel.getPixiHotelsByName(name, user, false));
    }

    @Override
    public PixiHotelI create(final @Nonnull UserI user, final @Nonnull PixiHotelI hotel) throws DataFormatException, ResourceAlreadyExistsException {
        log.debug("User {} is attempting to create hotel {}", user.getUsername(), hotel);

        validateNewHotel(hotel);

        try {
            hotel.setId(PixiHotel.CreateNewID());
        } catch (Exception e) {
            PixiRuntimeException pixiRuntimeException = new PixiRuntimeException("Unexpected error generating an id for " + PixiHotel.SCHEMA_ELEMENT_NAME + " as requested by " + user.getUsername(), e);
            log.error("", pixiRuntimeException);
            throw pixiRuntimeException;
        }

        XFTItem item = ((ItemI) hotel).getItem();

        // Save
        try {
            log.debug("Saving hotel.");
            SaveItemHelper.authorizedSave(item, user, false, false, false, true, EventUtils.DEFAULT_EVENT(user, "Created new " + item.getXSIType()));
            log.debug("Hotel saved.");
        } catch (Exception e) {
            PixiRuntimeException pixiRuntimeException = new PixiRuntimeException("The save operation failed for a new " + PixiHotel.SCHEMA_ELEMENT_NAME + " as requested by " + user.getUsername());
            log.error("", pixiRuntimeException);
            throw pixiRuntimeException;
        }

        return findByName(user, hotel.getName()).orElseThrow(() -> new PixiRuntimeException("Just created a new " + PixiHotel.SCHEMA_ELEMENT_NAME + " named " + hotel.getName() + " but now I'm unable to find it."));
    }

    @Override
    public PixiHotelI update(final UserI user, final String hotelName, final PixiHotelI hotel) throws DataFormatException, NotFoundException, ResourceAlreadyExistsException {
        log.debug("User {} is attempting to update hotel {}", user.getUsername(), hotel.getName());

        validateUpdatedHotel(hotelName, hotel);

        // Get the existing hotel
        PixiHotelI existingHotel = findByName(user, hotelName).orElseThrow(() -> new NotFoundException(PixiHotel.SCHEMA_ELEMENT_NAME, hotelName));
        // Set the id of the updated hotel to the existing id
        hotel.setId(existingHotel.getId());

        XFTItem item = ((ItemI) hotel).getItem();

        // Save
        try {
            log.debug("Updating hotel.");
            SaveItemHelper.authorizedSave(item, user, false, false, false, true, EventUtils.DEFAULT_EVENT(user, "Updated a " + item.getXSIType() + " named " + hotel.getName()));
            log.debug("Hotel updated.");
        } catch (Exception e) {
            PixiRuntimeException pixiRuntimeException = new PixiRuntimeException("The save operation failed when updating a " + PixiHotel.SCHEMA_ELEMENT_NAME + " named " + hotel.getName() + " as requested by " + user.getUsername());
            log.error("", pixiRuntimeException);
            throw pixiRuntimeException;
        }

        return findByName(user, hotel.getName()).orElseThrow(() -> new PixiRuntimeException("Just updated a " + PixiHotel.SCHEMA_ELEMENT_NAME + " named " + hotel.getName() + " but now I'm unable to find it."));
    }

    @Override
    public void delete(final UserI user, final String hotelName) throws NotFoundException, HotelReferenceException {
        if (hotelReferencedByHotelScanRecord(hotelName)) {
            throw new HotelReferenceException();
        }

        final PixiHotelI hotel = findByName(user, hotelName).orElseThrow(() -> new NotFoundException("The submitted hotel does not exist. Cannot delete."));
        XFTItem item = ((ItemI) hotel).getItem();
        try {
            SaveItemHelper.authorizedDelete(item, user, EventUtils.DEFAULT_EVENT(user, "Deleted hotel named " + hotelName) );
        } catch (Exception e) {
            throw new PixiRuntimeException("Error deleting hotel " + hotelName + " for user " + user.getUsername(), e);
        }
    }

    protected boolean hotelNameExists(final String hotelName) {
        return StringUtils.isNotBlank(hotelName) && jdbcTemplate.queryForObject(QUERY_HOTEL_NAME_EXISTS, new MapSqlParameterSource(PARAM_HOTEL_NAME, hotelName), Boolean.class);
    }

    protected boolean hotelReferencedByHotelScanRecord(final String hotelName) {
        return StringUtils.isNotBlank(hotelName) && jdbcTemplate.queryForObject(QUERY_HOTEL_NAME_REFERENCED_BY_HOTEL_SCAN_RECORD, new MapSqlParameterSource(PARAM_HOTEL_NAME, hotelName), Boolean.class);
    }

    protected void validateNewHotel(final PixiHotelI hotel) throws DataFormatException, ResourceAlreadyExistsException {
        // New hotels must have unique names
        if (hotelNameExists(hotel.getName())) {
            throw new ResourceAlreadyExistsException(PixiHotel.SCHEMA_ELEMENT_NAME, hotel.getName());
        }

        // Finish validation
        validateHotel(hotel);
    }

    protected void validateUpdatedHotel(final String hotelName, final PixiHotelI hotel) throws ResourceAlreadyExistsException, DataFormatException {
        // If changing the hotel name
        if (!hotelName.equals(hotel.getName())) {
            // See if the new hotel name is in use
            if (hotelNameExists(hotel.getName())) {
                // If it is cannot update to new name as hotel names should be unique
                throw new ResourceAlreadyExistsException(PixiHotel.SCHEMA_ELEMENT_NAME, hotel.getName());
            }
        }

        // Finish validation
        validateHotel(hotel);
    }

    protected void validateHotel(final PixiHotelI hotel) throws DataFormatException {
        log.debug("Validating hotel.");

        DataFormatException e = new DataFormatException();
        boolean isValid = true;


        // Must provide a hotel name
        if (hotel.getName() == null || StringUtils.isBlank(hotel.getName())) {
            e.addMissingField("hotel name");
            isValid = false;
        }

        // Hotels must have two positions
        List<PixiHotelpositionI> hotelPositions = hotel.getPositions_position();

        if (hotelPositions.size() < 2) {
            e.addInvalidField("positions - hotels must have at least two hotel positions.");
            isValid = false;
        }

        Set<String> hotelPositionNames = hotelPositions.stream()
                                                       .map(position -> position.getName().trim())
                                                       .collect(Collectors.toSet());

        // Hotel position names must be unique
        if (hotelPositionNames.size() != hotelPositions.size()) {
            e.addInvalidField("hotel position name - hotel positions must have unique names");
            isValid = false;
        }

        // Hotel positions must have names
        if (hotelPositionNames.contains(null) || hotelPositionNames.contains("")) {
            e.addInvalidField("hotel position name - all hotel positions must have a name.");
            isValid = false;
        }


        // Hotel positions X Y Z values must be greater than 0
        long negativeX = hotelPositions.stream().filter(position -> (Integer) position.getX() < 1).count();
        long negativeY = hotelPositions.stream().filter(position -> (Integer) position.getY() < 1).count();
        long negativeZ= hotelPositions.stream().filter(position -> (Integer) position.getZ() < 1).count();

        if (negativeX > 0 || negativeY > 0 || negativeZ > 0) {
            e.addInvalidField("x, y, z - all hotel position x, y, z values must be greater than zero.");
            isValid = false;
        }

        int uniqueXYZs = hotelPositions.stream()
                                       .map(position -> Arrays.asList((Integer) position.getX(), (Integer) position.getY(), (Integer) position.getZ()))
                                       .collect(Collectors.toSet())
                                       .size();

        if (uniqueXYZs != hotelPositions.size()) {
            e.addInvalidField("x, y, z - all hotel positions must have a unique x, y, and z values.");
            isValid = false;
        }

        if (!isValid) {
            log.error("", e);
            throw e;
        }

        // No errors then.
    }

}
