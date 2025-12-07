function execute(os, current_path, arguments)
    local nodes = os:getNodes(current_path)

    -- Sort alphabetically
    table.sort(nodes, function(a, b)
        return a:getName():lower() < b:getName():lower()
    end)

    for _, node in ipairs(nodes) do
        local name = node:getName()

        -- Add trailing slash if directory
        if node:isDirectory() then
            name = name .. "/"
        end

        os:print(name .. "\n")
    end

    os:print("\n")
end