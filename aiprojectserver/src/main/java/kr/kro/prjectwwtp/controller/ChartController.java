package kr.kro.prjectwwtp.controller;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/charts")
@Tag(name = "Chart", description = "차트 이미지 제공 API")
@Slf4j
public class ChartController {
	
	@Value("${chart.png.save-path:./charts}")
	private String chartSavePath;
	
	@GetMapping("/{filename:.+}")
	@Operation(summary = "차트 PNG 이미지 조회", description = "이메일에 포함된 차트 PNG 파일을 반환합니다")
	public ResponseEntity<Resource> getChartImage(@PathVariable String filename) {
		try {
			// 파일명이 valid한지 확인 (보안을 위해 경로 탐색 방지)
			if (filename == null || filename.isEmpty() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
				log.warn("[Chart] Invalid filename requested: {}", filename);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}
			
			// 파일 경로 설정
			Path filePath = Paths.get(chartSavePath, filename);
			File file = filePath.toFile();
			
			// 파일 존재 여부 확인
			if (!file.exists() || !file.isFile()) {
				log.warn("[Chart] File not found: {}", filePath.toAbsolutePath());
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
			
			// 파일이 chartSavePath 디렉토리 내에 있는지 확인 (보안)
			if (!filePath.toAbsolutePath().startsWith(Paths.get(chartSavePath).toAbsolutePath())) {
				log.warn("[Chart] Path traversal attempt detected: {}", filePath.toAbsolutePath());
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
			
			// 파일 타입 확인
			if (!filename.toLowerCase().endsWith(".png")) {
				log.warn("[Chart] Invalid file extension: {}", filename);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
			}
			
			Resource resource = new FileSystemResource(file);
			
			log.info("[Chart] Returning chart image: {}", filename);
			
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
					.contentType(MediaType.IMAGE_PNG)
					.body(resource);
					
		} catch (Exception e) {
			log.error("[Chart] Error retrieving chart image: {}", filename, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}
