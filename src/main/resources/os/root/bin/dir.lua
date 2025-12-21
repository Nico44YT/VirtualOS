function execute(current_path, arguments)
    local nodes = System:listDirectories(current_path)

    local cont = false

    ::top::

    for _, node in ipairs(nodes) do
        local name = node:getName()

        if node:isDirectory() then
            name = name .. "/"
        end

        System:print(name .. "\n")
    end

    cont = not cont

    if cont then
        nodes = System:listFiles(current_path)
        goto top
    end

    System:print("\n")
end