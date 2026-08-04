package com.example.booking.service;

import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.Role;
import com.example.booking.entity.Room;
import com.example.booking.entity.User;
import com.example.booking.exception.BookingConflictException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.RoomRepository;
import com.example.booking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private UserRepository userRepository;

    private BookingServiceImpl bookingService;

    private Room room;
    private User user;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(bookingRepository, roomRepository, userRepository);

        room = new Room("Conference Room A", 8);
        ReflectionTestUtils.setField(room, "id", 1L);

        user = new User("Ivan", "ivan@example.com", Role.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        start = LocalDateTime.of(2026, 8, 1, 10, 0);
        end = LocalDateTime.of(2026, 8, 1, 11, 0);
    }

    @Test
    void createBooking_savesWhenNoConflict() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findConflicting(1L, start, end, null)).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.createBooking(1L, 1L, start, end);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(result.getRoom()).isEqualTo(room);
        assertThat(result.getUser()).isEqualTo(user);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_throwsWhenRoomMissing() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(1L, 1L, start, end))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Room not found");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_throwsWhenUserMissing() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(1L, 1L, start, end))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void createBooking_throwsWhenConflicting() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Booking existing = new Booking(room, user, start, end, BookingStatus.CONFIRMED);
        when(bookingRepository.findConflicting(1L, start, end, null)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> bookingService.createBooking(1L, 1L, start, end))
                .isInstanceOf(BookingConflictException.class);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void cancelBooking_setsStatusCancelled() {
        Booking booking = new Booking(room, user, start, end, BookingStatus.CONFIRMED);
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.cancelBooking(5L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancelBooking_throwsWhenMissing() {
        when(bookingRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(5L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rescheduleBooking_updatesTimesWhenNoConflict() {
        Booking booking = new Booking(room, user, start, end, BookingStatus.CONFIRMED);
        ReflectionTestUtils.setField(booking, "id", 5L);
        LocalDateTime newStart = start.plusDays(1);
        LocalDateTime newEnd = end.plusDays(1);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findConflicting(eq(1L), eq(newStart), eq(newEnd), eq(5L)))
                .thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking result = bookingService.rescheduleBooking(5L, newStart, newEnd);

        assertThat(result.getStartTime()).isEqualTo(newStart);
        assertThat(result.getEndTime()).isEqualTo(newEnd);
    }

    @Test
    void rescheduleBooking_throwsWhenConflicting() {
        Booking booking = new Booking(room, user, start, end, BookingStatus.CONFIRMED);
        ReflectionTestUtils.setField(booking, "id", 5L);
        LocalDateTime newStart = start.plusDays(1);
        LocalDateTime newEnd = end.plusDays(1);
        Booking other = new Booking(room, user, newStart, newEnd, BookingStatus.CONFIRMED);

        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(bookingRepository.findConflicting(eq(1L), eq(newStart), eq(newEnd), eq(5L)))
                .thenReturn(List.of(other));

        assertThatThrownBy(() -> bookingService.rescheduleBooking(5L, newStart, newEnd))
                .isInstanceOf(BookingConflictException.class);
    }

    @Test
    void rescheduleBooking_throwsWhenMissing() {
        when(bookingRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.rescheduleBooking(5L, start, end))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
