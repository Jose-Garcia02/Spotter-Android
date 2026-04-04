import xml.etree.ElementTree as ET
def update_themes(file_path):
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        # Add M3 Theme items to Theme.AppGym or Base.Theme.AppGym
        # We will iterate and find Base.Theme.AppGym or Theme.AppGym
        for style in root.findall('style'):
            if style.attrib['name'] == 'Base.Theme.AppGym':
                # Check existing items and add if they don't exist
                m3_items = {
                    'colorPrimary': '@color/md_theme_primary',
                    'colorOnPrimary': '@color/md_theme_onPrimary',
                    'colorPrimaryContainer': '@color/md_theme_primaryContainer',
                    'colorOnPrimaryContainer': '@color/md_theme_onPrimaryContainer',
                    'colorSecondary': '@color/md_theme_secondary',
                    'colorOnSecondary': '@color/md_theme_onSecondary',
                    'colorSecondaryContainer': '@color/md_theme_secondaryContainer',
                    'colorOnSecondaryContainer': '@color/md_theme_onSecondaryContainer',
                    'colorTertiary': '@color/md_theme_secondary',
                    'colorError': '@color/md_theme_error',
                    'colorOnError': '@color/md_theme_onError',
                    'colorErrorContainer': '@color/md_theme_errorContainer',
                    'colorOnErrorContainer': '@color/md_theme_onErrorContainer',
                    'colorSurface': '@color/md_theme_surface',
                    'colorOnSurface': '@color/md_theme_onSurface',
                    'colorSurfaceVariant': '@color/md_theme_surfaceVariant',
                    'colorOnSurfaceVariant': '@color/md_theme_onSurfaceVariant',
                    'colorOutline': '@color/md_theme_outline',
                 }
                existing = {item.attrib['name'] for item in style.findall('item')}
                for name, value in m3_items.items():
                    if name not in existing:
                        elem = ET.Element('item', {'name': name})
                        elem.text = value
                        style.append(elem)
                    else:
                        for elem in style.findall('item'):
                            if elem.attrib['name'] == name:
                                elem.text = value
        tree.write(file_path, xml_declaration=True, encoding='utf-8')
    except Exception as e:
        print(f"Error: {e}")
update_themes('app/src/main/res/values/themes.xml')
