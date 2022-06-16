package org.nrg.xnatx.plugins.pixi.hotelsplitter.services;

import org.nrg.xapi.exceptions.DataFormatException;
import org.nrg.xapi.exceptions.NotFoundException;
import org.nrg.xapi.exceptions.ResourceAlreadyExistsException;
import org.nrg.xdat.model.PixiHotelI;
import org.nrg.xft.security.UserI;
import org.nrg.xnatx.plugins.pixi.hotelsplitter.exceptions.HotelReferenceException;

import java.util.List;
import java.util.Optional;

public interface HotelService {
    List<PixiHotelI> findAll(final UserI user);
    Optional<PixiHotelI> findByName(final UserI user, final String name);
    PixiHotelI create(final UserI user, final PixiHotelI hotel) throws DataFormatException, ResourceAlreadyExistsException;
    PixiHotelI update(final UserI user, final String hotelName, final PixiHotelI hotel) throws DataFormatException, NotFoundException, ResourceAlreadyExistsException;
    void delete(final UserI user, final String hotelName) throws NotFoundException, HotelReferenceException;
}
