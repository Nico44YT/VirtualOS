function execute(current_path, arguments)
    local ms = tonumber(arguments[1])
    if not ms then
        System:print("usage: sleep <milliseconds>\n")
        return
    end

    System:sleep(ms)
end
