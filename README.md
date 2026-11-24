# Vulkanium

Vulkanium is a fork of [Nvidium](https://github.com/MCRcortex/nvidium) that aims to run its GPU-driven,
mesh-shader terrain renderer for [Sodium](https://github.com/CaffeineMC/sodium) on Minecraft's Vulkan backend
using `VK_EXT_mesh_shader`, so it works on any GPU with mesh shader support instead of only NVIDIA.

**Status:** work in progress. It builds on the Vulkan port in progress on the `26.2-vk` branch of
[drouarb/nvidium](https://github.com/drouarb/nvidium) and is being brought up to Minecraft 26.3.

Known gaps (see git history for details): "Improved Transparency" (26.3's order independent transparency) is not
implemented on the Vulkan path yet, shader packs (Iris) are not supported, and nothing has been verified on real
hardware yet.

## Credits

This project exists because of other people's work and is a derivative of theirs:

- **Cortex ([MCRcortex](https://github.com/MCRcortex))**: original author of Nvidium and its mesh shader rendering design.
- **[drouarb](https://github.com/drouarb)**: maintainer of the Nvidium fork this repository is based on, and author of the Vulkan port (`26.2-vk`).
- Everyone else in the git history, including etil2jz, ferriarnus, Riflusso and Ethan Zeigler.
- [Sodium](https://github.com/CaffeineMC/sodium) by JellySquid, IMS212 and the CaffeineMC team. Vulkanium is a mod
  for Sodium and does not include Sodium's code (Sodium is under the Polyform Shield license).

Upstream history is preserved in this repository.

## License

LGPL-3.0, the same as Nvidium. See `LICENSE.txt`. Changes made in this fork are recorded in the git history.

---

# Nvidium (original README)

[![Modrinth](https://img.shields.io/modrinth/dt/nvidium?logo=modrinth)](https://modrinth.com/mod/nvidium)

Nvidium is an alternate rendering backing for sodium, it uses cutting edge nvidia features to render huge amounts of
terrain geometry at very playable framerates.

### Requires sodium and an nvidia gtx 1600 series or newer to run (turing+ architecture)