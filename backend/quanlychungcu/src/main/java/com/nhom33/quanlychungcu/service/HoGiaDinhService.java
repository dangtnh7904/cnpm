package com.nhom33.quanlychungcu.service;

import com.nhom33.quanlychungcu.entity.HoGiaDinh;
import com.nhom33.quanlychungcu.exception.ResourceNotFoundException;
import com.nhom33.quanlychungcu.repository.HoGiaDinhRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HoGiaDinhService {

    private final HoGiaDinhRepository repo;

    public HoGiaDinhService(HoGiaDinhRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public HoGiaDinh create(HoGiaDinh hoGiaDinh) {
        // Kiểm tra mã hộ gia đình đã tồn tại chưa
        if (repo.existsByMaHoGiaDinh(hoGiaDinh.getMaHoGiaDinh())) {
            throw new IllegalArgumentException(
                "Mã hộ gia đình '" + hoGiaDinh.getMaHoGiaDinh() + "' đã tồn tại"
            );
        }

        // Set ngày tạo sẽ được xử lý bởi @PrePersist
        return repo.save(hoGiaDinh);
    }

    @Transactional
    public HoGiaDinh update(@NonNull Integer id, HoGiaDinh updated) {
        HoGiaDinh exist = repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình với ID: " + id));

        // Kiểm tra nếu đổi mã hộ và mã mới đã tồn tại
        if (!exist.getMaHoGiaDinh().equals(updated.getMaHoGiaDinh()) 
            && repo.existsByMaHoGiaDinh(updated.getMaHoGiaDinh())) {
            throw new IllegalArgumentException(
                "Mã hộ gia đình '" + updated.getMaHoGiaDinh() + "' đã tồn tại"
            );
        }

        // Cập nhật thông tin
        exist.setMaHoGiaDinh(updated.getMaHoGiaDinh());
        exist.setTenChuHo(updated.getTenChuHo());
        exist.setSoDienThoaiLienHe(updated.getSoDienThoaiLienHe());
        exist.setEmailLienHe(updated.getEmailLienHe());
        exist.setSoTang(updated.getSoTang());
        exist.setSoCanHo(updated.getSoCanHo());
        exist.setDienTich(updated.getDienTich());
        exist.setTrangThai(updated.getTrangThai());

        // NgayCapNhat sẽ được set bởi @PreUpdate
        return repo.save(exist);
    }

    @Transactional
    public void delete(@NonNull Integer id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy hộ gia đình với ID: " + id);
        }
        repo.deleteById(id);
    }

    public HoGiaDinh getById(@NonNull Integer id) {
        return repo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình với ID: " + id));
    }

    public HoGiaDinh getByMaHoGiaDinh(String maHoGiaDinh) {
        return repo.findByMaHoGiaDinh(maHoGiaDinh)
            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hộ gia đình với mã: " + maHoGiaDinh));
    }

    public Page<HoGiaDinh> findAll(@NonNull Pageable pageable) {
        return repo.findAll(pageable);
    }

    public Page<HoGiaDinh> searchByTenChuHo(String tenChuHo, @NonNull Pageable pageable) {
        if (tenChuHo == null || tenChuHo.isBlank()) {
            return repo.findAll(pageable);
        }
        return repo.findByTenChuHoContainingIgnoreCase(tenChuHo, pageable);
    }

    public Page<HoGiaDinh> searchBySoCanHo(String soCanHo, @NonNull Pageable pageable) {
        return repo.findBySoCanHo(soCanHo, pageable);
    }

    public Page<HoGiaDinh> searchByTang(Integer soTang, @NonNull Pageable pageable) {
        return repo.findBySoTang(soTang, pageable);
    }

    public Page<HoGiaDinh> searchByTrangThai(String trangThai, @NonNull Pageable pageable) {
        return repo.findByTrangThai(trangThai, pageable);
    }

    public Page<HoGiaDinh> search(String maHoGiaDinh, String tenChuHo, String soCanHo, 
                                   String trangThai, @NonNull Pageable pageable) {
        return repo.search(maHoGiaDinh, tenChuHo, soCanHo, trangThai, pageable);
    }

    public long countByTrangThai(String trangThai) {
        return repo.countByTrangThai(trangThai);
    }
}
