function execute(os, current_path, arguments)
    local hz, duration = arguments:match("([^:]+) (.+)")
    os:tone(hz, duration)
end