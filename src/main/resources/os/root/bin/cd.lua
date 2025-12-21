function execute(current_path, arguments)
    local target

    if #arguments == 0 then
        target = "/"
    else
        target = arguments[1]
    end

    local success = System:setCwd(target)
    if not success then
        System:print("cd: no such directory: " .. target .. "\n")
    end
end