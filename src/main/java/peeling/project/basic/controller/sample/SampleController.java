package peeling.project.basic.controller.sample;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "샘플", description = "SampleController")
public class SampleController {

    private final RedisTemplate<String, String> redisTemplate;

    @Operation(summary = "레디스 테스트")
    @GetMapping("/redis-test")
    public String redisTest() {
        ValueOperations<String, String> vop = redisTemplate.opsForValue();
        vop.set("Korea", "Seoul");
        vop.set("America", "NewYork");
        vop.set("Italy", "Rome");
        vop.set("Japan", "Tokyo");
        return "aaa";
    }
}
