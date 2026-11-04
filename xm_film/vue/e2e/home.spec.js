import { expect, test } from '@playwright/test'

/**
 * 首页海报墙条数 = 网格实际列数 × 2，且末列不能越出网格。
 * 这条守的是「列数从布局读回」那段：列数算错一格，整墙就溢出一列。
 */
test('首页海报墙排成整两行且不溢出主栏', async ({ page }) => {
  await page.setViewportSize({ width: 1920, height: 1080 })
  await page.goto('/front/home')

  const grid = page.locator('.poster-grid').first()
  const cards = grid.locator('.poster-card')
  await expect(cards.first()).toBeVisible()

  const { cols, cardCount, rows, overflowPx } = await grid.evaluate((el) => {
    const cards = [...el.querySelectorAll('.poster-card')]
    const gridRight = el.getBoundingClientRect().right
    return {
      cols: getComputedStyle(el).gridTemplateColumns.split(' ').length,
      cardCount: cards.length,
      rows: new Set(cards.map((c) => Math.round(c.getBoundingClientRect().top))).size,
      overflowPx: Math.round(Math.max(...cards.map((c) => c.getBoundingClientRect().right)) - gridRight),
    }
  })

  // 演示数据不足 8 片时放宽：只校验不溢出
  if (cardCount >= 8) {
    expect(cardCount).toBe(Math.max(cols * 2, 8))
    expect(rows).toBe(2)
  }
  expect(overflowPx).toBeLessThanOrEqual(0)
})
