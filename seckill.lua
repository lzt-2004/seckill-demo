local stock = tonumber(redis.call('get', KEYS[1]))
local seckill = redis.call('sismember', KEYS[2], ARGV[1])
if seckill == 1 then
    return 2
else
    if stock > 0 then
        redis.call('decr', KEYS[1])
        redis.call('sadd', KEYS[2], ARGV[1])
        return 1
    else
        return 0
    end
end