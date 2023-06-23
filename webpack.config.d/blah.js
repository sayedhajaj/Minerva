config.resolve.modules.push("src/main/resources");

config.module.rules.push(
    {
        test: /\.(minerva)$/i,
        type: 'asset/source'
    }
);

